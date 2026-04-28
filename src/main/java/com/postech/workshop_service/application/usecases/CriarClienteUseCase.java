package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Endereco;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarClienteUseCase {

	private final ClienteRepository clienteRepository;

	/**
	 * Construtor para injeção de dependências.
	 * @param clienteRepository repositório de clientes.
	 */
	public CriarClienteUseCase(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	/**
	 * Executa o cadastro de um novo cliente no sistema.
	 * @param nome o nome completo ou razão social do cliente (obrigatório).
	 * @param documentoRaw o CPF ou CNPJ em formato string (pode conter pontuação).
	 * @param email o email de contato (opcional, desde que o telefone seja informado).
	 * @param telefone o telefone de contato (opcional, desde que o email seja informado).
	 * @return a entidade {@link Cliente} persistida.
	 * @throws IllegalArgumentException caso o documento já esteja cadastrado ou dados
	 * inválidos sejam enviados.
	 */
	@Transactional
	public Cliente executar(String nome, String documentoRaw, String email, String telefone) {
		return executar(nome, documentoRaw, email, telefone, null, null, null);
	}

	/**
	 * Executa o cadastro de um novo cliente no sistema com dados opcionais.
	 * @param nome o nome completo ou razão social do cliente (obrigatório).
	 * @param documentoRaw o CPF ou CNPJ em formato string (pode conter pontuação).
	 * @param email o email de contato (opcional, desde que o telefone seja informado).
	 * @param telefone o telefone de contato (opcional, desde que o email seja informado).
	 * @param endereco o endereço do cliente (opcional).
	 * @param dataNascimentoFundacao a data de nascimento ou fundação (opcional).
	 * @param observacoes observações adicionais (opcional).
	 * @return a entidade {@link Cliente} persistida.
	 * @throws IllegalArgumentException caso o documento já esteja cadastrado ou dados
	 * inválidos sejam enviados.
	 */
	@Transactional
	public Cliente executar(String nome, String documentoRaw, String email, String telefone, Endereco endereco,
			java.time.LocalDate dataNascimentoFundacao, String observacoes) {
		Documento documento = new Documento(documentoRaw);

		if (clienteRepository.existePorDocumento(documento.getValor())) {
			throw new IllegalArgumentException("Já existe um cliente cadastrado com este documento.");
		}

		Cliente cliente = new Cliente(null, nome, documento, email, telefone, endereco, dataNascimentoFundacao,
				observacoes);
		return clienteRepository.salvar(cliente);
	}

}
