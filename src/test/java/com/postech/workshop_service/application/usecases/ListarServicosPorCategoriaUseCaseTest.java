package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarServicosPorCategoriaUseCaseTest {

	@Mock
	private ServicoRepository servicoRepository;

	@InjectMocks
	private ListarServicosPorCategoriaUseCase listarServicosPorCategoriaUseCase;

	@Test
	void shouldReturnServicosByCategoria() {
		Servico servico = criarServico(UUID.randomUUID(), "Revisao completa", "Descricao", new BigDecimal("350.00"),
				CategoriaServico.PREVENTIVA);
		when(servicoRepository.listarPorCategoria(CategoriaServico.PREVENTIVA, false)).thenReturn(List.of(servico));

		List<Servico> resultado = listarServicosPorCategoriaUseCase.executar(CategoriaServico.PREVENTIVA, false);

		assertEquals(1, resultado.size());
		assertEquals(CategoriaServico.PREVENTIVA, resultado.get(0).getCategoria());
	}

	@Test
	void shouldIncludeInactivesWhenRequested() {
		when(servicoRepository.listarPorCategoria(CategoriaServico.MECANICA, true)).thenReturn(List.of());

		List<Servico> resultado = listarServicosPorCategoriaUseCase.executar(CategoriaServico.MECANICA, true);

		assertTrue(resultado.isEmpty());
	}

	private Servico criarServico(UUID id, String nome, String descricao, BigDecimal valor, CategoriaServico categoria) {
		return new Servico(id, nome, descricao, valor, categoria, null, null, null);
	}

}
