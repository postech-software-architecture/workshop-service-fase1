package com.postech.workshop_service.domain.valueobjects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentoTest {

	@Test
	void shouldCreateCpfWhen11DigitsAreProvided() {
		// Path rest < 2 for both digits (digit=0)
		Documento documento = new Documento("98765432100");
		assertEquals("98765432100", documento.getValor());

		// Path rest >= 2 for both digits (digit = 11 - rest)
		Documento doc2 = new Documento("45678912364");
		assertEquals("45678912364", doc2.getValor());

		// Path rest < 2 for first digit only
		Documento doc3 = new Documento("12345678909");
		assertEquals("12345678909", doc3.getValor());
	}

	@Test
	void shouldCreateCnpjWhen14DigitsAreProvided() {
		// Path d < 10 for both digits (Ex: 11.222.333/0001-81)
		Documento doc1 = new Documento("11222333000181");
		assertEquals("11222333000181", doc1.getValor());

		// Path d >= 10 for one of the digits (Ex: 10000000000307 where d1=0)
		Documento doc2 = new Documento("10000000000307");
		assertEquals("10000000000307", doc2.getValor());

		// Path d >= 10 for second digit (Ex: 10000000000650 where d2=0)
		Documento doc3 = new Documento("10000000000650");
		assertEquals("10000000000650", doc3.getValor());
	}

	@Test
	void shouldThrowExceptionWhenCpfIsInvalid() {
		// CPF com dígitos verificadores errados
		assertThrows(IllegalArgumentException.class, () -> new Documento("98765432111"));
		// CPF com todos os dígitos iguais
		assertThrows(IllegalArgumentException.class, () -> new Documento("11111111111"));
	}

	@Test
	void shouldThrowExceptionWhenCnpjIsInvalid() {
		// CNPJ com dígitos verificadores errados
		assertThrows(IllegalArgumentException.class, () -> new Documento("11222333000100"));
		// CNPJ com todos os dígitos iguais
		assertThrows(IllegalArgumentException.class, () -> new Documento("11111111111111"));
	}

	@Test
	void shouldThrowExceptionWhenLengthIsInvalid() {
		assertThrows(IllegalArgumentException.class, () -> new Documento("123"));
		assertThrows(IllegalArgumentException.class, () -> new Documento("123456789012"));
	}

	@Test
	void shouldThrowExceptionWhenNull() {
		assertThrows(IllegalArgumentException.class, () -> new Documento(null));
	}

	@Test
	void shouldMaskCpfCorrectly() {
		Documento documento = new Documento("98765432100");
		assertEquals("***.654.321-**", documento.mascarado());
	}

	@Test
	void shouldMaskCnpjCorrectly() {
		Documento documento = new Documento("11222333000181");
		assertEquals("**.*22.***/0001-**", documento.mascarado());
	}

	@Test
	void shouldHandleEqualsAndHashCode() {
		Documento d1 = new Documento("98765432100");
		Documento d2 = new Documento("987.654.321-00");
		Documento d3 = new Documento("11222333000181");

		assertEquals(d1, d2);
		assertNotEquals(d1, d3);
		assertNotEquals(d1, null);
		assertNotEquals(d1, "string");
		assertEquals(d1.hashCode(), d2.hashCode());
	}

	@Test
	void shouldCatchExceptionInValidation() {
		// Forçar um erro dentro do try/catch dos validadores se possível
		// Mas a lógica atual já está bem protegida.
		// Vou apenas garantir que letras não quebram (replaceAll remove elas)
		Documento d1 = new Documento("987.654.321-00A");
		assertEquals("98765432100", d1.getValor());
	}

}
