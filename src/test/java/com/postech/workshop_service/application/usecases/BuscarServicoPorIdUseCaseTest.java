package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
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
class BuscarServicoPorIdUseCaseTest {

	@Mock
	private ServicoRepository servicoRepository;

	@InjectMocks
	private BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;

	@Test
	void shouldReturnServicoWhenFound() {
		UUID id = UUID.randomUUID();
		Servico servico = criarServico(id, "Troca de oleo", "Descricao", new BigDecimal("100.00"), 60);
		when(servicoRepository.buscarPorId(id)).thenReturn(Optional.of(servico));

		assertTrue(buscarServicoPorIdUseCase.executar(id).isPresent());
	}

	@Test
	void shouldReturnEmptyWhenNotFound() {
		UUID id = UUID.randomUUID();
		when(servicoRepository.buscarPorId(id)).thenReturn(Optional.empty());

		assertTrue(buscarServicoPorIdUseCase.executar(id).isEmpty());
	}

	private Servico criarServico(UUID id, String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos) {
		return new Servico(id, nome, descricao, valor, tempoEstimadoMinutos, null, null, null, null);
	}

}
