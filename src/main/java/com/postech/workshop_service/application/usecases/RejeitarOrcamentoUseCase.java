package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por registrar a rejeicao de um orcamento.
 */
@Service
public class RejeitarOrcamentoUseCase {

	private final OrcamentoRepository orcamentoRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	private final PecaInsumoRepository pecaInsumoRepository;

	private final MecanicoNotificationService mecanicoNotificationService;

	/**
	 * Construtor para injecao das dependencias do caso de uso.
	 * @param orcamentoRepository repositorio de orcamentos.
	 * @param ordemServicoRepository repositorio de ordens.
	 * @param estoqueRepository repositorio de estoques.
	 * @param movimentacaoEstoqueRepository repositorio de movimentacoes.
	 * @param pecaInsumoRepository repositorio de pecas.
	 * @param mecanicoNotificationService service de notificacao do mecanico.
	 */
	public RejeitarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
			OrdemServicoRepository ordemServicoRepository, EstoqueRepository estoqueRepository,
			MovimentacaoEstoqueRepository movimentacaoEstoqueRepository, PecaInsumoRepository pecaInsumoRepository,
			MecanicoNotificationService mecanicoNotificationService) {
		this.orcamentoRepository = orcamentoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
		this.pecaInsumoRepository = pecaInsumoRepository;
		this.mecanicoNotificationService = mecanicoNotificationService;
	}

	/**
	 * Registra a rejeicao de um orcamento pendente.
	 * @param idOrcamento identificador do orcamento.
	 * @return orcamento rejeitado.
	 */
	@Transactional
	public Orcamento executar(UUID idOrcamento) {
		Orcamento orcamento = orcamentoRepository.buscarPorId(idOrcamento)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Orcamento nao encontrado."));
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));

		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE) {
			throw new RegraDeNegocioException(
					"A ordem de servico deve estar aguardando resposta do cliente para rejeitar o orcamento.");
		}

		orcamento.rejeitar();
		ordemServico.voltarParaComposicao();

		liberarReservasDeEstoque(ordemServico);

		ordemServicoRepository.salvar(ordemServico);
		Orcamento orcamentoPersistido = orcamentoRepository.salvar(orcamento);
		mecanicoNotificationService.notificarAtualizacaoOrcamento(ordemServico, orcamentoPersistido);
		return orcamentoPersistido;
	}

	private void liberarReservasDeEstoque(OrdemServico ordemServico) {
		String motivo = "Liberacao de reserva — orcamento rejeitado OS " + ordemServico.getNumero();
		ordemServico.getItensComposicao()
			.stream()
			.filter(item -> item.getTipo() == TipoItemComposicaoTecnica.PECA && item.getIdPecaInsumo() != null)
			.forEach(item -> {
				PecaInsumo peca = pecaInsumoRepository.buscarPorId(item.getIdPecaInsumo(), true).orElse(null);
				if (peca == null || peca.getValorUnitario().compareTo(BigDecimal.ZERO) == 0) {
					return;
				}
				BigDecimal quantidade = item.getValor().divide(peca.getValorUnitario(), 3, RoundingMode.HALF_UP);
				List<Estoque> estoques = estoqueRepository.listarPorPeca(item.getIdPecaInsumo(), false);
				if (estoques.isEmpty()) {
					return;
				}
				Estoque estoque = estoques.get(0);
				MovimentacaoEstoque mov = estoque.liberarReserva(quantidade, motivo);
				estoqueRepository.salvar(estoque);
				movimentacaoEstoqueRepository.salvar(mov);
			});
	}

}
