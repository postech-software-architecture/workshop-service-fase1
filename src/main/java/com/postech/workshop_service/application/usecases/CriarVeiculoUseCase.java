package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por cadastrar um novo veiculo.
 */
@Service
public class CriarVeiculoUseCase {

	private final VeiculoRepository veiculoRepository;

	private final ClienteRepository clienteRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param veiculoRepository repositorio de veiculos.
	 * @param clienteRepository repositorio de clientes.
	 */
	public CriarVeiculoUseCase(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
		this.veiculoRepository = veiculoRepository;
		this.clienteRepository = clienteRepository;
	}

	/**
	 * Executa o cadastro de um novo veiculo.
	 * @param placaRaw placa informada.
	 * @param marca marca informada.
	 * @param modelo modelo informado.
	 * @param ano ano informado.
	 * @param cor cor opcional.
	 * @param observacoes observacoes opcionais.
	 * @param clientesIds clientes vinculados.
	 * @return veiculo persistido.
	 */
	@Transactional
	public Veiculo executar(String placaRaw, String marca, String modelo, int ano, String cor, String observacoes,
			List<UUID> clientesIds) {
		try {
			Placa placa = new Placa(placaRaw);
			validarClientes(clientesIds);

			if (veiculoRepository.existePlacaAtiva(placa.getValor(), null)) {
				throw new RegraDeNegocioException("Ja existe um veiculo ativo cadastrado com a placa informada.");
			}

			Veiculo veiculo = new Veiculo(null, placa, marca, modelo, ano, cor, observacoes, clientesIds);

			return veiculoRepository.salvar(veiculo);
		}
		catch (RegraDeNegocioException ex) {
			throw ex;
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

	private void validarClientes(List<UUID> clientesIds) {
		if (clientesIds == null || clientesIds.isEmpty()) {
			throw new RegraDeNegocioException("O veiculo deve possuir ao menos um cliente vinculado.");
		}
		for (UUID clienteId : clientesIds) {
			if (clienteRepository.buscarPorId(clienteId, false).isEmpty()) {
				throw new RegraDeNegocioException("Todos os clientes vinculados devem existir previamente.");
			}
		}
	}

}
