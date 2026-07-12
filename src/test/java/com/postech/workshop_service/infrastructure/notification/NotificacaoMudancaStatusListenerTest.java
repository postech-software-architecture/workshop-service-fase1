package com.postech.workshop_service.infrastructure.notification;

import com.postech.workshop_service.application.usecases.ClienteNotificationService;
import com.postech.workshop_service.application.usecases.MudancaStatusOrdemServicoEvent;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoMudancaStatusListenerTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private ClienteNotificationService clienteNotificationService;

	@InjectMocks
	private NotificacaoMudancaStatusListener listener;

	@Test
	void shouldNotifyClientWhenOrderExists() {
		UUID ordemId = UUID.randomUUID();
		OrdemServico ordemServico = criarOrdemServico(ordemId);
		when(ordemServicoRepository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemServico));

		listener.aoMudarStatus(new MudancaStatusOrdemServicoEvent(ordemId, StatusOrdemServico.RECEBIDO,
				StatusOrdemServico.EM_DIAGNOSTICO));

		verify(clienteNotificationService).notificarMudancaStatus(ordemServico, StatusOrdemServico.RECEBIDO,
				StatusOrdemServico.EM_DIAGNOSTICO);
	}

	@Test
	void shouldSkipWhenOrderNotFound() {
		UUID ordemId = UUID.randomUUID();
		when(ordemServicoRepository.buscarPorId(ordemId)).thenReturn(Optional.empty());

		listener.aoMudarStatus(new MudancaStatusOrdemServicoEvent(ordemId, StatusOrdemServico.RECEBIDO,
				StatusOrdemServico.EM_DIAGNOSTICO));

		verify(clienteNotificationService, never()).notificarMudancaStatus(any(), any(), any());
	}

	@Test
	void shouldSwallowNotificationFailure() {
		UUID ordemId = UUID.randomUUID();
		OrdemServico ordemServico = criarOrdemServico(ordemId);
		when(ordemServicoRepository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemServico));
		doThrow(new RuntimeException("falha de canal")).when(clienteNotificationService)
			.notificarMudancaStatus(any(), any(), any());

		assertDoesNotThrow(() -> listener.aoMudarStatus(new MudancaStatusOrdemServicoEvent(ordemId,
				StatusOrdemServico.RECEBIDO, StatusOrdemServico.EM_DIAGNOSTICO)));
	}

	private OrdemServico criarOrdemServico(UUID ordemId) {
		return new OrdemServico(ordemId, UUID.randomUUID(), UUID.randomUUID(), StatusOrdemServico.EM_DIAGNOSTICO,
				List.of(), "OS-2026-00001", null, LocalDateTime.now().minusDays(1), LocalDateTime.now(), null);
	}

}
