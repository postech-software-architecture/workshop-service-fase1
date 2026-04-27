package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso responsável por listar clientes de forma paginada e retornar o total de registros.
 */
@Service
public class ListarClientesUseCase {

    private final ClienteRepository clienteRepository;

    /**
     * Construtor para injeção de dependências.
     *
     * @param clienteRepository repositório de clientes.
     */
    public ListarClientesUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Lista clientes com base na paginação informada.
     *
     * @param pagina número da página (inicia em 0).
     * @param tamanho quantidade de registros por página.
     * @return lista de {@link Cliente} encontrados.
     */
    public List<Cliente> executar(int pagina, int tamanho) {
        return clienteRepository.listar(pagina, tamanho);
    }

    /**
     * Retorna o total de clientes cadastrados no sistema.
     *
     * @return quantidade total de registros.
     */
    public long contarTotal() {
        return clienteRepository.contarTodos();
    }
}
