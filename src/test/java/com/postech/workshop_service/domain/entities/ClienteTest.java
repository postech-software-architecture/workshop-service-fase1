package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

	@Test
	void shouldCreateClienteWithValidData() {
		UUID id = UUID.randomUUID();
		Documento doc = new Documento("98765432100");
		Cliente cliente = new Cliente(id, "Nome", doc, "email@email.com", "11999999999");

		assertEquals(id, cliente.getId());
		assertEquals("Nome", cliente.getNome());
		assertEquals(doc, cliente.getDocumento());
		assertNotNull(cliente.getDataCriacao());
		assertEquals(cliente.getDataCriacao(), cliente.getDataUltimaAtualizacao());
	}

	@Test
	void shouldThrowExceptionWhenNomeIsInvalid() {
		Documento doc = new Documento("98765432100");
		assertThrows(IllegalArgumentException.class, () -> new Cliente(null, null, doc, "e", "t"));
		assertThrows(IllegalArgumentException.class, () -> new Cliente(null, " ", doc, "e", "t"));
	}

	@Test
	void shouldThrowExceptionWhenDocumentoIsNull() {
		assertThrows(IllegalArgumentException.class, () -> new Cliente(null, "Nome", null, "e", "t"));
	}

	@Test
	void shouldThrowExceptionWhenContactsAreMissing() {
		Documento doc = new Documento("98765432100");
		assertThrows(IllegalArgumentException.class, () -> new Cliente(null, "Nome", doc, null, null));
		assertThrows(IllegalArgumentException.class, () -> new Cliente(null, "Nome", doc, " ", " "));
		assertThrows(IllegalArgumentException.class, () -> new Cliente(null, "Nome", doc, null, " "));
		assertThrows(IllegalArgumentException.class, () -> new Cliente(null, "Nome", doc, " ", null));
	}

	@Test
	void shouldAcceptOneContact() {
		Documento doc = new Documento("98765432100");
		assertNotNull(new Cliente(null, "Nome", doc, "email", null));
		assertNotNull(new Cliente(null, "Nome", doc, null, "11"));
		assertNotNull(new Cliente(null, "Nome", doc, "email", "11"));
	}

	@Test
	void shouldUpdateData() {
		Cliente cliente = new Cliente(null, "Original", new Documento("98765432100"), "e@e.com", null);
		Endereco endereco = new Endereco("Rua", "1", null, "B", "C", "SP", "01234567");
		LocalDate data = LocalDate.of(1990, 1, 1);

		cliente.atualizarDados("Novo", "novo@e.com", "11", endereco, data, "Obs");

		assertEquals("Novo", cliente.getNome());
		assertEquals("novo@e.com", cliente.getEmail());
		assertEquals("11", cliente.getTelefone());
		assertEquals(endereco, cliente.getEndereco());
		assertEquals(data, cliente.getDataNascimentoFundacao());
		assertEquals("Obs", cliente.getObservacoes());
		assertTrue(cliente.getDataUltimaAtualizacao().isAfter(cliente.getDataCriacao())
				|| cliente.getDataUltimaAtualizacao().isEqual(cliente.getDataCriacao()));
	}

	@Test
	void shouldThrowExceptionWhenUpdatingWithInvalidData() {
		Cliente cliente = new Cliente(null, "Original", new Documento("98765432100"), "e@e.com", null);

		assertThrows(IllegalArgumentException.class, () -> cliente.atualizarDados("", "e", "t", null, null, null));

		assertThrows(IllegalArgumentException.class, () -> cliente.atualizarDados("Novo", "", "", null, null, null));
	}

}
