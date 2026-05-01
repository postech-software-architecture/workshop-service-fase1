package com.postech.workshop_service.domain.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VeiculoTest {

	@Test
	void shouldCreateVeiculoWithValidData() {
		UUID clienteId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, " Prata ", " Obs ", List.of(clienteId));

		assertNotNull(veiculo.getId());
		assertEquals("BRA1D23", veiculo.getPlaca().getValor());
		assertEquals("Prata", veiculo.getCor());
		assertEquals("Obs", veiculo.getObservacoes());
		assertTrue(veiculo.isAtivo());
		assertEquals(1, veiculo.getClientesVinculados().size());
	}

	@Test
	void shouldRebuildPersistedVeiculo() {
		UUID veiculoId = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		LocalDateTime criacao = LocalDateTime.now().minusDays(2);
		LocalDateTime atualizacao = LocalDateTime.now().minusDays(1);
		LocalDateTime remocao = LocalDateTime.now();

		Veiculo veiculo = new Veiculo(veiculoId, "BRA1D23", "Toyota", "Corolla", 2020, "Prata", "Obs",
				List.of(clienteId), false, criacao, atualizacao, remocao);

		assertEquals(veiculoId, veiculo.getId());
		assertFalse(veiculo.isAtivo());
		assertEquals(criacao, veiculo.getDataCriacao());
		assertEquals(atualizacao, veiculo.getDataUltimaAtualizacao());
		assertEquals(remocao, veiculo.getDataRemocao());
	}

	@Test
	void shouldRejectInvalidCadastroData() {
		UUID clienteId = UUID.randomUUID();

		assertThrows(IllegalArgumentException.class,
				() -> new Veiculo(null, "Toyota", "Corolla", 2020, null, null, List.of(clienteId)));
		assertThrows(IllegalArgumentException.class,
				() -> new Veiculo("BRA1D23", " ", "Corolla", 2020, null, null, List.of(clienteId)));
		assertThrows(IllegalArgumentException.class,
				() -> new Veiculo("BRA1D23", "Toyota", " ", 2020, null, null, List.of(clienteId)));
		assertThrows(IllegalArgumentException.class,
				() -> new Veiculo("BRA1D23", "Toyota", "Corolla", 1899, null, null, List.of(clienteId)));
	}

	@Test
	void shouldRejectInvalidClientesCollection() {
		UUID clienteId = UUID.randomUUID();

		assertThrows(IllegalArgumentException.class,
				() -> new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of()));
		assertThrows(IllegalArgumentException.class,
				() -> new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId, clienteId)));
		assertThrows(IllegalArgumentException.class,
				() -> new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, Arrays.asList(clienteId, null)));
	}

	@Test
	void shouldUpdateOnlyVehicleData() {
		UUID clienteId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId));
		LocalDateTime dataCriacao = veiculo.getDataCriacao();
		LocalDateTime dataAnterior = veiculo.getDataUltimaAtualizacao();

		veiculo.atualizarDados("ABC1234", " Ford ", " Focus ", 2018, " Azul ", " Atualizado ");

		assertEquals("ABC1234", veiculo.getPlaca().getValor());
		assertEquals("Ford", veiculo.getMarca());
		assertEquals("Focus", veiculo.getModelo());
		assertEquals(2018, veiculo.getAno());
		assertEquals("Azul", veiculo.getCor());
		assertEquals("Atualizado", veiculo.getObservacoes());
		assertEquals(dataCriacao, veiculo.getDataCriacao());
		assertTrue(veiculo.getDataUltimaAtualizacao().isAfter(dataAnterior)
				|| veiculo.getDataUltimaAtualizacao().isEqual(dataAnterior));
		assertEquals(List.of(clienteId), veiculo.getClientesVinculados().stream().toList());
	}

	@Test
	void shouldRejectInvalidDataWhenUpdatingVehicle() {
		UUID clienteId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId));

		assertThrows(IllegalArgumentException.class,
				() -> veiculo.atualizarDados(null, "Ford", "Focus", 2018, null, "Obs"));
		assertThrows(IllegalArgumentException.class,
				() -> veiculo.atualizarDados("ABC1234", " ", "Focus", 2018, null, "Obs"));
		assertThrows(IllegalArgumentException.class,
				() -> veiculo.atualizarDados("ABC1234", "Ford", " ", 2018, null, "Obs"));
		assertThrows(IllegalArgumentException.class,
				() -> veiculo.atualizarDados("ABC1234", "Ford", "Focus", 1899, null, "Obs"));
	}

	@Test
	void shouldManageClienteLinks() {
		UUID clienteA = UUID.randomUUID();
		UUID clienteB = UUID.randomUUID();
		Veiculo veiculo = new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteA));

		veiculo.vincularCliente(clienteB);
		assertEquals(List.of(clienteA, clienteB), veiculo.getClientesVinculados().stream().toList());

		veiculo.desvincularCliente(clienteB);
		assertEquals(List.of(clienteA), veiculo.getClientesVinculados().stream().toList());
	}

	@Test
	void shouldRejectInvalidClienteLinkOperations() {
		UUID clienteId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId));

		assertThrows(IllegalArgumentException.class, () -> veiculo.vincularCliente(null));
		assertThrows(IllegalArgumentException.class, () -> veiculo.vincularCliente(clienteId));
		assertThrows(IllegalArgumentException.class, () -> veiculo.desvincularCliente(null));
		assertThrows(IllegalArgumentException.class, () -> veiculo.desvincularCliente(UUID.randomUUID()));
		assertThrows(IllegalArgumentException.class, () -> veiculo.desvincularCliente(clienteId));
	}

	@Test
	void shouldUpdateAndRemoveLogically() {
		UUID clienteId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, "   ", "   ", List.of(clienteId));

		assertNull(veiculo.getCor());
		assertNull(veiculo.getObservacoes());

		veiculo.removerLogicamente();
		assertFalse(veiculo.isAtivo());
		assertNotNull(veiculo.getDataRemocao());

		LocalDateTime remocao = veiculo.getDataRemocao();
		veiculo.removerLogicamente();
		assertEquals(remocao, veiculo.getDataRemocao());
	}

}
