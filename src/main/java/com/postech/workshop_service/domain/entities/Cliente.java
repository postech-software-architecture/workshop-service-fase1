package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.Documento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade raiz (Aggregate Root) que representa um Cliente do sistema.
 */
@Getter
@Builder
@AllArgsConstructor
public class Cliente {
    private final UUID id;
    private String nome;
    private final Documento documento; // Imutável
    private String email;
    private String telefone;
    private Endereco endereco;
    private LocalDate dataNascimentoFundacao;
    private String observacoes;
    private final LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;

    /**
     * Constrói um novo cliente (Cadastro Inicial).
     */
    public Cliente(UUID id, String nome, Documento documento, String email, String telefone) {
        validarNome(nome);
        validarDocumento(documento);
        validarContatos(email, telefone);
        
        this.id = id != null ? id : UUID.randomUUID();
        this.nome = nome;
        this.documento = documento;
        this.email = email;
        this.telefone = telefone;
        this.dataCriacao = LocalDateTime.now();
        this.dataUltimaAtualizacao = this.dataCriacao;
    }

    public void atualizarDados(String nome, String email, String telefone, Endereco endereco, 
                               LocalDate dataNascimentoFundacao, String observacoes) {
        validarNome(nome);
        validarContatos(email, telefone);
        
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.dataNascimentoFundacao = dataNascimentoFundacao;
        this.observacoes = observacoes;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    private void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
    }

    private void validarDocumento(Documento documento) {
        if (documento == null) {
            throw new IllegalArgumentException("O documento de identificação é obrigatório.");
        }
    }

    private void validarContatos(String email, String telefone) {
        if ((email == null || email.trim().isEmpty()) && (telefone == null || telefone.trim().isEmpty())) {
            throw new IllegalArgumentException("É obrigatório informar pelo menos um e-mail ou telefone de contato.");
        }
    }
}
