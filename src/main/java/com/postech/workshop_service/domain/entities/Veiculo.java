package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.Placa;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entidade raiz que representa um veiculo da oficina.
 */
@Getter
public class Veiculo {

    private static final int ANO_MINIMO = 1900;

    private final UUID id;
    private Placa placa;
    private String marca;
    private String modelo;
    private int ano;
    private String cor;
    private String observacoes;
    private final Set<UUID> clientesVinculados;
    private boolean ativo;
    private final LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;
    private LocalDateTime dataRemocao;

    /**
     * Cria um novo veiculo com os dados operacionais obrigatorios.
     *
     * @param id identificador tecnico do veiculo.
     * @param placa placa do veiculo.
     * @param marca marca do veiculo.
     * @param modelo modelo do veiculo.
     * @param ano ano do veiculo.
     * @param cor cor opcional do veiculo.
     * @param observacoes observacoes opcionais.
     * @param clientesVinculados clientes associados ao veiculo.
     */
    public Veiculo(UUID id,
                   Placa placa,
                   String marca,
                   String modelo,
                   int ano,
                   String cor,
                   String observacoes,
                   Collection<UUID> clientesVinculados) {
        this.id = id != null ? id : UUID.randomUUID();
        this.dataCriacao = LocalDateTime.now();
        this.dataUltimaAtualizacao = this.dataCriacao;
        this.clientesVinculados = new LinkedHashSet<>();
        this.ativo = true;

        aplicarDados(placa, marca, modelo, ano, cor, observacoes);
        definirClientesIniciais(clientesVinculados);
    }

    /**
     * Reconstroi um veiculo previamente persistido.
     *
     * @param id identificador tecnico do veiculo.
     * @param placa placa do veiculo.
     * @param marca marca do veiculo.
     * @param modelo modelo do veiculo.
     * @param ano ano do veiculo.
     * @param cor cor opcional do veiculo.
     * @param observacoes observacoes opcionais.
     * @param clientesVinculados clientes associados ao veiculo.
     * @param ativo indicador operacional do veiculo.
     * @param dataCriacao data de criacao.
     * @param dataUltimaAtualizacao data da ultima atualizacao.
     * @param dataRemocao data da remocao logica.
     */
    public Veiculo(UUID id,
                   Placa placa,
                   String marca,
                   String modelo,
                   int ano,
                   String cor,
                   String observacoes,
                   Collection<UUID> clientesVinculados,
                   boolean ativo,
                   LocalDateTime dataCriacao,
                   LocalDateTime dataUltimaAtualizacao,
                   LocalDateTime dataRemocao) {
        this.id = Objects.requireNonNull(id, "O identificador do veiculo e obrigatorio.");
        this.dataCriacao = Objects.requireNonNull(dataCriacao, "A data de criacao do veiculo e obrigatoria.");
        this.dataUltimaAtualizacao = Objects.requireNonNull(dataUltimaAtualizacao, "A data de ultima atualizacao do veiculo e obrigatoria.");
        this.dataRemocao = dataRemocao;
        this.clientesVinculados = new LinkedHashSet<>();
        this.ativo = ativo;

        aplicarDados(placa, marca, modelo, ano, cor, observacoes);
        definirClientesIniciais(clientesVinculados);
    }

    /**
     * Atualiza apenas os dados cadastrais do veiculo sem alterar os clientes vinculados.
     *
     * @param placa nova placa.
     * @param marca nova marca.
     * @param modelo novo modelo.
     * @param ano novo ano.
     * @param cor nova cor.
     * @param observacoes novas observacoes.
     */
    public void atualizarDados(Placa placa,
                               String marca,
                               String modelo,
                               int ano,
                               String cor,
                               String observacoes) {
        aplicarDados(placa, marca, modelo, ano, cor, observacoes);
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    /**
     * Vincula um novo cliente ao veiculo.
     *
     * @param clienteId identificador do cliente.
     */
    public void vincularCliente(UUID clienteId) {
        validarClienteNaoNulo(clienteId);
        if (!this.clientesVinculados.add(clienteId)) {
            throw new IllegalArgumentException("O cliente informado ja esta vinculado ao veiculo.");
        }
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    /**
     * Desvincula um cliente existente do veiculo.
     *
     * @param clienteId identificador do cliente.
     */
    public void desvincularCliente(UUID clienteId) {
        validarClienteNaoNulo(clienteId);
        if (!this.clientesVinculados.contains(clienteId)) {
            throw new IllegalArgumentException("O cliente informado nao esta vinculado ao veiculo.");
        }
        if (this.clientesVinculados.size() == 1) {
            throw new IllegalArgumentException("O veiculo deve possuir ao menos um cliente vinculado.");
        }
        this.clientesVinculados.remove(clienteId);
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    /**
     * Executa a remocao logica do veiculo.
     */
    public void removerLogicamente() {
        if (!this.ativo) {
            return;
        }
        this.ativo = false;
        this.dataRemocao = LocalDateTime.now();
        this.dataUltimaAtualizacao = this.dataRemocao;
    }

    private void aplicarDados(Placa placa,
                              String marca,
                              String modelo,
                              int ano,
                              String cor,
                              String observacoes) {
        if (placa == null) {
            throw new IllegalArgumentException("A placa do veiculo e obrigatoria.");
        }
        if (ano < ANO_MINIMO || ano > LocalDateTime.now().getYear()) {
            throw new IllegalArgumentException("O ano do veiculo esta fora da faixa permitida.");
        }
        this.placa = placa;
        this.marca = sanitizarObrigatorio(marca, "A marca do veiculo e obrigatoria.");
        this.modelo = sanitizarObrigatorio(modelo, "O modelo do veiculo e obrigatorio.");
        this.ano = ano;
        this.cor = sanitizarOpcional(cor);
        this.observacoes = sanitizarOpcional(observacoes);
    }

    private String sanitizarObrigatorio(String valor, String mensagem) {
        String sanitizado = sanitizarOpcional(valor);
        if (sanitizado == null) {
            throw new IllegalArgumentException(mensagem);
        }
        return sanitizado;
    }

    private void validarClienteNaoNulo(UUID clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("Nao e permitido informar cliente nulo no vinculo do veiculo.");
        }
    }

    private void definirClientesIniciais(Collection<UUID> clientesVinculados) {
        if (clientesVinculados == null || clientesVinculados.isEmpty()) {
            throw new IllegalArgumentException("O veiculo deve possuir ao menos um cliente vinculado.");
        }

        LinkedHashSet<UUID> conjunto = new LinkedHashSet<>();
        for (UUID clienteId : clientesVinculados) {
            validarClienteNaoNulo(clienteId);
            conjunto.add(clienteId);
        }

        if (conjunto.size() != clientesVinculados.size()) {
            throw new IllegalArgumentException("Nao e permitido repetir clientes vinculados ao mesmo veiculo.");
        }

        this.clientesVinculados.addAll(conjunto);
    }

    private String sanitizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String sanitizado = valor.trim().replaceAll("\\s+", " ");
        return sanitizado.isEmpty() ? null : sanitizado;
    }
}
