package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por vincular um novo cliente a um veiculo existente.
 */
@Service
public class VincularClienteVeiculoUseCase {

	private final VeiculoRepository veiculoRepository;

	private final ClienteRepository clienteRepository;

	public VincularClienteVeiculoUseCase(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
		this.veiculoRepository = veiculoRepository;
		this.clienteRepository = clienteRepository;
	}

	/**
	 * Vincula um cliente existente ao veiculo informado.
	 * @param veiculoId identificador do veiculo.
	 * @param clienteId identificador do cliente.
	 * @return veiculo atualizado.
	 */
	@Transactional
	public Veiculo executar(UUID veiculoId, UUID clienteId) {
		Veiculo veiculo = veiculoRepository.buscarPorId(veiculoId, true)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado com o ID informado."));

		if (clienteRepository.buscarPorId(clienteId, false).isEmpty()) {
			throw new RegraDeNegocioException("O cliente informado deve existir previamente.");
		}

		try {
			veiculo.vincularCliente(clienteId);
			return veiculoRepository.salvar(veiculo);
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

}
