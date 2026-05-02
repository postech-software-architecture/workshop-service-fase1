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
 * Caso de uso responsavel por registrar a aprovacao de um orcamento.
 */
@Service
public class AprovarOrcamentoUseCase {

	private final OrcamentoRepository orcamentoRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	private final MecanicoNotificationService mecanicoNotificationService;

	/**
	 * Construtor para injecao das dependencias do caso de uso.
	 * @param orcamentoRepository repositorio de orcamentos.
	 * @param ordemServicoRepository repositorio de ordens.
	 * @param mecanicoNotificationService service de notificacao do mecanico.
	 */
	public AprovarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
			OrdemServicoRepository ordemServicoRepository, MecanicoNotificationService mecanicoNotificationService) {
		this.orcamentoRepository = orcamentoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.mecanicoNotificationService = mecanicoNotificationService;
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
		orcamento.aprovar(ordemServico);

		ordemServicoRepository.salvar(ordemServico);
		Orcamento orcamentoPersistido = orcamentoRepository.salvar(orcamento);
		mecanicoNotificationService.notificarAtualizacaoOrcamento(ordemServico, orcamentoPersistido);
		return orcamentoPersistido;
	}

	private OrdemServico buscarOrdemVinculada(Orcamento orcamento) {
		return ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
	}

	private void validarOrdemAguardandoResposta(OrdemServico ordemServico) {
		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE) {
			throw new RegraDeNegocioException(
					"A ordem de servico deve estar aguardando resposta do cliente para aprovar o orcamento.");
		}
	}

}
