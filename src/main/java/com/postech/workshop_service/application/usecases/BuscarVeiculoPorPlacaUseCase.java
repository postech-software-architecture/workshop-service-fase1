package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Caso de uso responsavel por buscar um veiculo pela placa.
 */
@Service
public class BuscarVeiculoPorPlacaUseCase {

	private final VeiculoRepository veiculoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param veiculoRepository repositorio de veiculos.
	 */
	public BuscarVeiculoPorPlacaUseCase(VeiculoRepository veiculoRepository) {
		this.veiculoRepository = veiculoRepository;
	}

	/**
	 * Busca um veiculo pela placa informada.
	 * @param placaRaw placa a ser consultada.
	 * @param incluirInativos indica se veiculos inativos devem ser considerados.
	 * @return veiculo encontrado, se existir.
	 */
	public Optional<Veiculo> executar(String placaRaw, boolean incluirInativos) {
		return veiculoRepository.buscarPorPlaca(Placa.normalizar(placaRaw), incluirInativos);
	}

}
