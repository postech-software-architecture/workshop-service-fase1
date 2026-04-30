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
				120, CategoriaServico.PREVENTIVA);
		when(servicoRepository.listarPorCategoria(CategoriaServico.PREVENTIVA, false)).thenReturn(List.of(servico));

		List<Servico> resultado = listarServicosPorCategoriaUseCase.executar(CategoriaServico.PREVENTIVA, false);

		assertEquals(1, resultado.size());
		assertEquals(CategoriaServico.PREVENTIVA, resultado.get(0).getCategoria());
	}

	private Servico criarServico(UUID id, String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos,
			CategoriaServico categoria) {
		return new Servico(id, nome, descricao, valor, tempoEstimadoMinutos, categoria, null, null, null);
	}

}
