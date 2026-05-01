package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarEstoquePorIdUseCaseTest {

	@Mock
	private EstoqueRepository estoqueRepository;

	@InjectMocks
	private BuscarEstoquePorIdUseCase buscarEstoquePorIdUseCase;

	@Test
	void deveRetornarEstoqueQuandoEncontrado() {
		UUID estoqueId = UUID.randomUUID();
		Estoque estoque = new Estoque(estoqueId, UUID.randomUUID(), "Prateleira A1", new BigDecimal("10"));
		when(estoqueRepository.buscarPorId(estoqueId, false)).thenReturn(Optional.of(estoque));

		assertTrue(buscarEstoquePorIdUseCase.executar(estoqueId, false).isPresent());
	}

}
