package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por atualizar um veiculo existente.
 */
@Service
public class AtualizarVeiculoUseCase {

	private final VeiculoRepository veiculoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param veiculoRepository repositorio de veiculos.
	 */
	public AtualizarVeiculoUseCase(VeiculoRepository veiculoRepository) {
		this.veiculoRepository = veiculoRepository;
	}

	/**
	 * Executa a atualizacao de um veiculo existente.
	 * @param id identificador do veiculo.
	 * @param placaRaw placa informada.
	 * @param marca marca informada.
	 * @param modelo modelo informado.
	 * @param ano ano informado.
	 * @param cor cor opcional.
	 * @param observacoes observacoes opcionais.
	 * @return veiculo atualizado.
	 */
	@Transactional
	public Veiculo executar(UUID id, String placaRaw, String marca, String modelo, int ano, String cor,
			String observacoes) {
		Veiculo veiculo = veiculoRepository.buscarPorId(id, true)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado com o ID informado."));

		try {
			if (veiculoRepository.existePlacaAtiva(Placa.normalizar(placaRaw), id)) {
				throw new RegraDeNegocioException("Já existe um veículo ativo cadastrado com a placa informada.");
			}

			veiculo.atualizarDados(placaRaw, marca, modelo, ano, cor, observacoes);

			return veiculoRepository.salvar(veiculo);
		}
		catch (RegraDeNegocioException ex) {
			throw ex;
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

}
