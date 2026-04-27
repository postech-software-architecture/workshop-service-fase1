package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Endereco;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AtualizarClienteUseCase {

    private final ClienteRepository clienteRepository;

    /**
     * Construtor para injeção de dependências.
     *
     * @param clienteRepository repositório de clientes.
     */
    public AtualizarClienteUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Executa a atualização dos dados cadastrais de um cliente existente.
     *
     * @param id identificador único do cliente.
     * @param nome novo nome ou razão social.
     * @param email novo e-mail.
     * @param telefone novo telefone.
     * @param endereco novo endereço completo.
     * @param dataNascimentoFundacao nova data de nascimento ou fundação.
     * @param observacoes novas observações.
     * @return a entidade {@link Cliente} atualizada.
     * @throws IllegalArgumentException caso o cliente não seja encontrado.
     */
    @Transactional
    public Cliente executar(UUID id, String nome, String email, String telefone, Endereco endereco, 
                           LocalDate dataNascimentoFundacao, String observacoes) {
        
        Cliente cliente = clienteRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com o ID informado."));

        cliente.atualizarDados(nome, email, telefone, endereco, dataNascimentoFundacao, observacoes);
        
        return clienteRepository.salvar(cliente);
    }
}
