package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarEstoquesPorPecaUseCaseTest {

	@Mock
	private EstoqueRepository estoqueRepository;

	@InjectMocks
	private ListarEstoquesPorPecaUseCase listarEstoquesPorPecaUseCase;

	@Test
	void deveListarEstoquesDaPecaInformada() {
		UUID pecaId = UUID.randomUUID();
		List<Estoque> estoques = List.of(new Estoque(UUID.randomUUID(), pecaId, "Prateleira A1", new BigDecimal("10")),
				new Estoque(UUID.randomUUID(), pecaId, "Prateleira B2", new BigDecimal("5")));
		when(estoqueRepository.listarPorPeca(pecaId, false)).thenReturn(estoques);

		List<Estoque> resultado = listarEstoquesPorPecaUseCase.executar(pecaId, false);

		assertEquals(2, resultado.size());
		assertEquals("Prateleira A1", resultado.get(0).getLocalizacao());
		assertEquals("Prateleira B2", resultado.get(1).getLocalizacao());
	}

}
