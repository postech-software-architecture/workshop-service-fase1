package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por registrar a rejeicao de um orcamento.
 */
@Service
public class RejeitarOrcamentoUseCase {

	private final OrcamentoRepository orcamentoRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	private final MecanicoNotificationService mecanicoNotificationService;

	/**
	 * Construtor para injecao das dependencias do caso de uso.
	 * @param orcamentoRepository repositorio de orcamentos.
	 * @param ordemServicoRepository repositorio de ordens.
	 * @param mecanicoNotificationService service de notificacao do mecanico.
	 */
	public RejeitarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
			OrdemServicoRepository ordemServicoRepository, MecanicoNotificationService mecanicoNotificationService) {
		this.orcamentoRepository = orcamentoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
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

		ordemServicoRepository.salvar(ordemServico);
		Orcamento orcamentoPersistido = orcamentoRepository.salvar(orcamento);
		mecanicoNotificationService.notificarAtualizacaoOrcamento(ordemServico, orcamentoPersistido);
		return orcamentoPersistido;
	}

}
