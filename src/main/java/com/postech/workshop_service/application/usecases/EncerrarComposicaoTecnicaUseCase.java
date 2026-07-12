package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.ItemOrcamento;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.TipoOrcamento;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por encerrar a composicao tecnica da ordem e gerar um orcamento
 * pendente de aprovacao.
 */
@Service
public class EncerrarComposicaoTecnicaUseCase {

	private static final Logger log = LoggerFactory.getLogger(EncerrarComposicaoTecnicaUseCase.class);

	private final OrdemServicoRepository ordemServicoRepository;

	private final OrcamentoRepository orcamentoRepository;

	private final ClienteNotificationService clienteNotificationService;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	/**
	 * Construtor para injecao das dependencias do caso de uso.
	 * @param ordemServicoRepository repositorio de ordens de servico.
	 * @param orcamentoRepository repositorio de orcamentos.
	 * @param clienteNotificationService service de notificacao do cliente.
	 */
	public EncerrarComposicaoTecnicaUseCase(OrdemServicoRepository ordemServicoRepository,
			OrcamentoRepository orcamentoRepository, ClienteNotificationService clienteNotificationService,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.orcamentoRepository = orcamentoRepository;
		this.clienteNotificationService = clienteNotificationService;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
	}

	/**
	 * Encerra a composicao tecnica da ordem e cria um orcamento pendente de aprovacao.
	 * @param idOrdemServico identificador da ordem de servico.
	 * @return orcamento gerado para a ordem.
	 */
	@Transactional
	public Orcamento executar(UUID idOrdemServico) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));

		if (!ordemServico.possuiItensComposicao()) {
			throw new RegraDeNegocioException(
					"Nao e permitido encerrar a composicao tecnica de uma ordem de servico sem itens.");
		}
		if (orcamentoRepository.existePendenteAprovacaoPorOrdemServico(idOrdemServico)) {
			throw new RegraDeNegocioException(
					"Nao e permitido gerar um novo orcamento quando ja existe outro pendente de aprovacao.");
		}

		List<ItemOrcamento> itensFotografados = ordemServico.getItensComposicao()
			.stream()
			.map(this::copiarItem)
			.toList();
		BigDecimal valorTotal = itensFotografados.stream()
			.map(ItemOrcamento::getValor)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		Orcamento orcamento = new Orcamento(null, ordemServico.getId(), valorTotal, itensFotografados,
				TipoOrcamento.SERVICO_ORIGINAL);
		orcamento.enviarParaAprovacao();
		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		ordemServico.encerrarComposicao();

		ordemServicoRepository.salvar(ordemServico);
		registrarHistoricoUseCase.executar(ordemServico.getId(), statusAnterior, ordemServico.getStatus());
		Orcamento orcamentoPersistido = orcamentoRepository.salvar(orcamento);
		try {
			clienteNotificationService.notificarOrcamentoPendente(ordemServico, orcamentoPersistido);
		}
		catch (RuntimeException ex) {
			log.warn("Falha ao notificar cliente sobre orcamento pendente da OS {}: {}", ordemServico.getNumero(),
					ex.getMessage());
		}
		return orcamentoPersistido;
	}

	private ItemOrcamento copiarItem(ItemComposicaoTecnica itemComposicaoTecnica) {
		return new ItemOrcamento(itemComposicaoTecnica.getDescricao(), itemComposicaoTecnica.getValor());
	}

}
