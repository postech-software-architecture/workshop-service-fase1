package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Caso de uso responsável por buscar um cliente pelo seu documento (CPF ou CNPJ).
 */
@Service
public class BuscarClientePorDocumentoUseCase {

    private final ClienteRepository clienteRepository;

    /**
     * Construtor para injeção de dependências.
     *
     * @param clienteRepository repositório de clientes.
     */
    public BuscarClientePorDocumentoUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Busca um cliente pelo seu documento.
     * Realiza a limpeza de caracteres não numéricos antes da consulta.
     *
     * @param documento número do documento (CPF/CNPJ).
     * @return um {@link Optional} contendo o cliente se encontrado, ou vazio caso contrário.
     */
    public Optional<Cliente> executar(String documento) {
        String cleanDoc = documento.replaceAll("[^0-9]", "");
        return clienteRepository.buscarPorDocumento(cleanDoc);
    }
}
