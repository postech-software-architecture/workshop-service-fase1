package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por desvincular um cliente de um veiculo existente.
 */
@Service
public class DesvincularClienteVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;

    public DesvincularClienteVeiculoUseCase(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    /**
     * Desvincula um cliente existente do veiculo informado.
     *
     * @param veiculoId identificador do veiculo.
     * @param clienteId identificador do cliente.
     * @return veiculo atualizado.
     */
    @Transactional
    public Veiculo executar(UUID veiculoId, UUID clienteId) {
        Veiculo veiculo = veiculoRepository.buscarPorId(veiculoId, true)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado com o ID informado."));

        try {
            veiculo.desvincularCliente(clienteId);
            return veiculoRepository.salvar(veiculo);
        } catch (IllegalArgumentException ex) {
            throw new RegraDeNegocioException(ex.getMessage());
        }
    }
}
