package com.postech.workshop_service.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlacaTest {

	@Test
	void shouldNormalizeValidMercosulPlate() {
		Placa placa = new Placa("bra-1d23");
		assertEquals("BRA1D23", placa.getValor());
	}

	@Test
	void shouldAcceptValidLegacyPlate() {
		Placa placa = new Placa("ABC1234");
		assertEquals("ABC1234", placa.getValor());
	}

	@Test
	void shouldMaskNormalizedPlate() {
		assertEquals("***1D**", new Placa("BRA1D23").mascarada());
		assertEquals("***12**", new Placa("ABC1234").mascarada());
	}

	@Test
	void shouldRejectInvalidPlate() {
		assertThrows(IllegalArgumentException.class, () -> new Placa("1234ABC"));
	}

}
