package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso responsável por buscar um cliente pelo seu identificador único (UUID).
 */
@Service
public class BuscarClientePorIdUseCase {

	private final ClienteRepository clienteRepository;

	/**
	 * Construtor para injeção de dependências.
	 * @param clienteRepository repositório de clientes.
	 */
	public BuscarClientePorIdUseCase(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	/**
	 * Busca um cliente pelo seu ID.
	 * @param id identificador único do cliente.
	 * @return um {@link Optional} contendo o cliente se encontrado, ou vazio caso
	 * contrário.
	 */
	public Optional<Cliente> executar(UUID id) {
		return clienteRepository.buscarPorId(id);
	}

}
