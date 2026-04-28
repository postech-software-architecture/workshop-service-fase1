package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RemoverClienteUseCase {

	private final ClienteRepository clienteRepository;

	/**
	 * Construtor para injeção de dependências.
	 * @param clienteRepository repositório de clientes.
	 */
	public RemoverClienteUseCase(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	/**
	 * Executa a remoção de um cliente do sistema.
	 * @param id identificador único do cliente a ser removido.
	 * @throws IllegalArgumentException caso o cliente não seja encontrado.
	 */
	@Transactional
	public void executar(UUID id) {
		Cliente cliente = clienteRepository.buscarPorId(id, true)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com o ID informado."));
		cliente.removerLogicamente();
		clienteRepository.salvar(cliente);
	}

}
