package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
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
class ListarServicosUseCaseTest {

	@Mock
	private ServicoRepository servicoRepository;

	@InjectMocks
	private ListarServicosUseCase listarServicosUseCase;

	@Test
	void shouldReturnPaginatedServicos() {
		Servico servico = criarServico(UUID.randomUUID(), "Troca de oleo", "Descricao", new BigDecimal("100.00"), 60);
		when(servicoRepository.listar(0, 20, null, null, false))
			.thenReturn(new PaginaResultado<>(List.of(servico), 1, 1, 0, 20));

		PaginaResultado<Servico> resultado = listarServicosUseCase.executar(0, 20, null, null, false);

		assertEquals(1, resultado.totalElementos());
		assertEquals(1, resultado.itens().size());
		assertEquals("Troca de oleo", resultado.itens().get(0).getNome());
	}

	private Servico criarServico(UUID id, String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos) {
		return new Servico(id, nome, descricao, valor, tempoEstimadoMinutos, null, null, null, null);
	}

}
