package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por remover logicamente um veiculo.
 */
@Service
public class RemoverVeiculoUseCase {

	private final VeiculoRepository veiculoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param veiculoRepository repositorio de veiculos.
	 */
	public RemoverVeiculoUseCase(VeiculoRepository veiculoRepository) {
		this.veiculoRepository = veiculoRepository;
	}

	/**
	 * Remove logicamente um veiculo preservando seu historico.
	 * @param id identificador do veiculo.
	 */
	@Transactional
	public void executar(UUID id) {
		var veiculo = veiculoRepository.buscarPorId(id, true)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado com o ID informado."));
		veiculo.removerLogicamente();
		veiculoRepository.salvar(veiculo);
	}

}
