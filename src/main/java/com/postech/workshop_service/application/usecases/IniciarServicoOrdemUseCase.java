package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por iniciar a execucao individual de um item de servico dentro
 * de uma ordem de servico em andamento.
 */
@Service
public class IniciarServicoOrdemUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	public IniciarServicoOrdemUseCase(OrdemServicoRepository ordemServicoRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
	}

	@Transactional
	public OrdemServico executar(UUID idOrdemServico, UUID idItem) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		ordemServico.iniciarServico(idItem);
		return ordemServicoRepository.salvar(ordemServico);
	}

}
