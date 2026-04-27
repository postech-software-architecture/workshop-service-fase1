package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por listar veiculos vinculados a um cliente.
 */
@Service
public class ListarVeiculosPorClienteUseCase {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    /**
     * Construtor para injecao de dependencias.
     *
     * @param veiculoRepository repositorio de veiculos.
     * @param clienteRepository repositorio de clientes.
     */
    public ListarVeiculosPorClienteUseCase(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Lista os veiculos vinculados a um cliente.
     *
     * @param clienteId identificador do cliente.
     * @param incluirInativos indica se veiculos inativos devem ser considerados.
     * @return lista de veiculos encontrados.
     */
    public List<Veiculo> executar(UUID clienteId, boolean incluirInativos) {
        if (clienteRepository.buscarPorId(clienteId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Cliente nao encontrado com o ID informado.");
        }
        return veiculoRepository.listarPorCliente(clienteId, incluirInativos);
    }
}
