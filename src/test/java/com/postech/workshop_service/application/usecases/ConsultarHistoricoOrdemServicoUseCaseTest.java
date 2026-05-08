package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarHistoricoOrdemServicoUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private HistoricoStatusOrdemServicoRepository historicoRepository;

	@InjectMocks
	private ConsultarHistoricoOrdemServicoUseCase useCase;

	@Test
	void shouldReturnOrderHistory() {
		OrdemServico ordemServico = criarOrdemServico();
		HistoricoStatusOrdemServico historico = new HistoricoStatusOrdemServico(null, ordemServico.getId(),
				StatusOrdemServico.AGUARDANDO_EXECUCAO, StatusOrdemServico.EM_EXECUCAO, LocalDateTime.now(),
				UUID.randomUUID(), "mecanico");
		when(ordemServicoRepository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
		when(historicoRepository.listarPorOrdemServico(ordemServico.getId())).thenReturn(List.of(historico));

		List<HistoricoStatusOrdemServico> resultado = useCase.executar(ordemServico.getId());

		assertEquals(List.of(historico), resultado);
	}

	@Test
	void shouldReturnNotFoundWhenOrderDoesNotExist() {
		UUID id = UUID.randomUUID();
		when(ordemServicoRepository.buscarPorId(id)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(id));
	}

	private OrdemServico criarOrdemServico() {
		return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				StatusOrdemServico.AGUARDANDO_EXECUCAO, List.of(), "OS-2026-00001", null,
				LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

}
