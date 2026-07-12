package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por registrar a aprovacao de um orcamento.
 */
@Service
public class AprovarOrcamentoUseCase {

	private static final Logger log = LoggerFactory.getLogger(AprovarOrcamentoUseCase.class);

	private final OrcamentoRepository orcamentoRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	private final MecanicoNotificationService mecanicoNotificationService;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	public AprovarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
			OrdemServicoRepository ordemServicoRepository, MecanicoNotificationService mecanicoNotificationService,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase, EstoqueRepository estoqueRepository,
			MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
		this.orcamentoRepository = orcamentoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.mecanicoNotificationService = mecanicoNotificationService;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
	}

	/**
	 * Registra a aprovacao de um orcamento pendente.
	 * @param idOrcamento identificador do orcamento.
	 * @return orcamento aprovado.
	 */
	@Transactional
	public Orcamento executar(UUID idOrcamento) {
		Orcamento orcamento = orcamentoRepository.buscarPorId(idOrcamento)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Orcamento nao encontrado."));
		OrdemServico ordemServico = buscarOrdemVinculada(orcamento);

		validarOrdemAguardandoResposta(ordemServico);
		validarOrcamentoPendente(orcamento);

		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		orcamento.aprovar(ordemServico);

		ordemServicoRepository.salvar(ordemServico);
		registrarHistoricoUseCase.executar(ordemServico.getId(), statusAnterior, ordemServico.getStatus());
		Orcamento orcamentoPersistido = orcamentoRepository.salvar(orcamento);
		consumirReservas(ordemServico);
		try {
			mecanicoNotificationService.notificarAtualizacaoOrcamento(ordemServico, orcamentoPersistido);
		}
		catch (RuntimeException ex) {
			log.warn("Falha ao notificar mecanico sobre aprovacao do orcamento da OS {}: {}", ordemServico.getNumero(),
					ex.getMessage());
		}
		return orcamentoPersistido;
	}

	private OrdemServico buscarOrdemVinculada(Orcamento orcamento) {
		return ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
	}

	private void validarOrdemAguardandoResposta(OrdemServico ordemServico) {
		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
			throw new RegraDeNegocioException(
					"A ordem de servico deve estar aguardando resposta do cliente para aprovar o orcamento.");
		}
	}

	private void validarOrcamentoPendente(Orcamento orcamento) {
		if (orcamento.getStatus() != com.postech.workshop_service.domain.entities.StatusOrcamento.PENDENTE_APROVACAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido aprovar um orcamento com status " + orcamento.getStatus() + ".");
		}
	}

	private void consumirReservas(OrdemServico ordemServico) {
		List<MovimentacaoEstoque> reservas = movimentacaoEstoqueRepository.listarPorOrdemServico(ordemServico.getId())
			.stream()
			.filter(mov -> mov.getTipo() == TipoMovimentacao.RESERVA)
			.toList();
		String motivo = "Baixa por aprovacao de orcamento da OS " + ordemServico.getNumero();
		for (MovimentacaoEstoque reserva : reservas) {
			Estoque estoque = estoqueRepository.buscarPorId(reserva.getEstoqueId(), true)
				.orElseThrow(
						() -> new RecursoNaoEncontradoException("Estoque nao encontrado para consumo da reserva."));
			MovimentacaoEstoque saida = estoque.consumirReserva(reserva.getQuantidade(), motivo, ordemServico.getId(),
					reserva.getOrcamentoId());
			movimentacaoEstoqueRepository.salvar(saida);
		}
	}

}
