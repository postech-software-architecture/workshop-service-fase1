package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.Placa;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidade raiz que representa um veiculo da oficina.
 */
@Getter
public class Veiculo extends EntidadeBase {

	private static final int ANO_MINIMO = 1900;

	private Placa placa;

	private String marca;

	private String modelo;

	private int ano;

	private String cor;

	private String observacoes;

	private final Set<UUID> clientesVinculados;

	private boolean ativo;

	/**
	 * Cria um novo veiculo com os dados operacionais obrigatorios.
	 * @param placaRaw placa do veiculo.
	 * @param marca marca do veiculo.
	 * @param modelo modelo do veiculo.
	 * @param ano ano do veiculo.
	 * @param cor cor opcional do veiculo.
	 * @param observacoes observacoes opcionais.
	 * @param clientesVinculados clientes associados ao veiculo.
	 */
	public Veiculo(String placaRaw, String marca, String modelo, int ano, String cor, String observacoes,
			Collection<UUID> clientesVinculados) {
		super(UUID.randomUUID());
		this.clientesVinculados = new LinkedHashSet<>();
		this.ativo = true;

		validarAno(ano);
		this.placa = new Placa(placaRaw);
		this.marca = sanitizarObrigatorio(marca, "A marca do veículo é obrigatória.");
		this.modelo = sanitizarObrigatorio(modelo, "O modelo do veículo é obrigatório.");
		this.ano = ano;
		this.cor = sanitizarOpcional(cor);
		this.observacoes = sanitizarOpcional(observacoes);
		definirClientesIniciais(clientesVinculados);
	}

	/**
	 * Reconstroi um veiculo previamente persistido.
	 * @param id identificador tecnico do veiculo.
	 * @param placaRaw placa do veiculo.
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
	@Default
	public Veiculo(UUID id, String placaRaw, String marca, String modelo, int ano, String cor, String observacoes,
			Collection<UUID> clientesVinculados, boolean ativo, LocalDateTime dataCriacao,
			LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.clientesVinculados = new LinkedHashSet<>();
		this.ativo = ativo;

		validarAno(ano);
		this.placa = new Placa(placaRaw);
		this.marca = sanitizarObrigatorio(marca, "A marca do veículo é obrigatória.");
		this.modelo = sanitizarObrigatorio(modelo, "O modelo do veículo é obrigatório.");
		this.ano = ano;
		this.cor = sanitizarOpcional(cor);
		this.observacoes = sanitizarOpcional(observacoes);
		definirClientesIniciais(clientesVinculados);
	}

	/**
	 * Atualiza apenas os dados cadastrais do veiculo sem alterar os clientes vinculados.
	 * @param placaRaw nova placa.
	 * @param marca nova marca.
	 * @param modelo novo modelo.
	 * @param ano novo ano.
	 * @param cor nova cor.
	 * @param observacoes novas observacoes.
	 */
	public void atualizarDados(String placaRaw, String marca, String modelo, int ano, String cor, String observacoes) {
		validarAno(ano);
		this.placa = new Placa(placaRaw);
		this.marca = sanitizarObrigatorio(marca, "A marca do veículo é obrigatória.");
		this.modelo = sanitizarObrigatorio(modelo, "O modelo do veículo é obrigatório.");
		this.ano = ano;
		this.cor = sanitizarOpcional(cor);
		this.observacoes = sanitizarOpcional(observacoes);
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Vincula um novo cliente ao veiculo.
	 * @param clienteId identificador do cliente.
	 */
	public void vincularCliente(UUID clienteId) {
		validarClienteNaoNulo(clienteId);
		if (!this.clientesVinculados.add(clienteId)) {
			throw new IllegalArgumentException("O cliente informado já está vinculado ao veículo.");
		}
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Desvincula um cliente existente do veiculo.
	 * @param clienteId identificador do cliente.
	 */
	public void desvincularCliente(UUID clienteId) {
		validarClienteNaoNulo(clienteId);
		if (!this.clientesVinculados.contains(clienteId)) {
			throw new IllegalArgumentException("O cliente informado não está vinculado ao veículo.");
		}
		if (this.clientesVinculados.size() == 1) {
			throw new IllegalArgumentException("O veículo deve possuir ao menos um cliente vinculado.");
		}
		this.clientesVinculados.remove(clienteId);
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Executa a remocao logica do veiculo.
	 */
	public void removerLogicamente() {
		if (!this.ativo) {
			return;
		}
		this.ativo = false;
		registrarRemocaoLogica();
	}

	private void validarAno(int ano) {
		if (ano < ANO_MINIMO || ano > LocalDateTime.now().getYear()) {
			throw new IllegalArgumentException("O ano do veículo está fora da faixa permitida.");
		}
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
			throw new IllegalArgumentException("Não é permitido informar cliente nulo no vínculo do veículo.");
		}
	}

	private void definirClientesIniciais(Collection<UUID> clientesVinculados) {
		if (clientesVinculados == null || clientesVinculados.isEmpty()) {
			throw new IllegalArgumentException("O veículo deve possuir ao menos um cliente vinculado.");
		}

		LinkedHashSet<UUID> conjunto = new LinkedHashSet<>();
		for (UUID clienteId : clientesVinculados) {
			validarClienteNaoNulo(clienteId);
			conjunto.add(clienteId);
		}

		if (conjunto.size() != clientesVinculados.size()) {
			throw new IllegalArgumentException("Não é permitido repetir clientes vinculados ao mesmo veículo.");
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
