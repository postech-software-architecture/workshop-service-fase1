package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso responsavel por buscar um veiculo pelo identificador.
 */
@Service
public class BuscarVeiculoPorIdUseCase {

    private final VeiculoRepository veiculoRepository;

    /**
     * Construtor para injecao de dependencias.
     *
     * @param veiculoRepository repositorio de veiculos.
     */
    public BuscarVeiculoPorIdUseCase(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    /**
     * Busca um veiculo pelo identificador.
     *
     * @param id identificador do veiculo.
     * @param incluirInativos indica se veiculos inativos devem ser considerados.
     * @return veiculo encontrado, se existir.
     */
    public Optional<Veiculo> executar(UUID id, boolean incluirInativos) {
        return veiculoRepository.buscarPorId(id, incluirInativos);
    }
}
