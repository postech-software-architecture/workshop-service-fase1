package com.postech.workshop_service.application.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContaInativaExceptionTest {

	@Test
	void devePreservarMensagem() {
		ContaInativaException exception = new ContaInativaException("Conta inativa.");

		assertEquals("Conta inativa.", exception.getMessage());
	}

}
