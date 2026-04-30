package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso responsavel por buscar um servico pelo identificador.
 */
@Service
public class BuscarServicoPorIdUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param servicoRepository repositorio de servicos.
	 */
	public BuscarServicoPorIdUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Busca um servico pelo seu identificador tecnico.
	 * @param id identificador do servico.
	 * @return servico encontrado, se existir.
	 */
	public Optional<Servico> executar(UUID id) {
		return servicoRepository.buscarPorId(id);
	}

}
