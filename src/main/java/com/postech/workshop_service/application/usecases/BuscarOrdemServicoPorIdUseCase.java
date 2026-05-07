package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso responsavel por recuperar uma ordem de servico pelo identificador tecnico.
 */
@Service
public class BuscarOrdemServicoPorIdUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param ordemServicoRepository repositorio de ordens de servico.
	 */
	public BuscarOrdemServicoPorIdUseCase(OrdemServicoRepository ordemServicoRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
	}

	/**
	 * Recupera a ordem de servico identificada.
	 * @param id identificador tecnico da ordem.
	 * @return ordem de servico encontrada.
	 * @throws RecursoNaoEncontradoException quando a ordem nao existir.
	 */
	public OrdemServico executar(UUID id) {
		return ordemServicoRepository.buscarPorId(id)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
	}

}
