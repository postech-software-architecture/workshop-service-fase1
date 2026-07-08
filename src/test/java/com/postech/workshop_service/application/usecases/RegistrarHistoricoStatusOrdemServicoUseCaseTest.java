package com.postech.workshop_service.application.usecases;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarHistoricoStatusOrdemServicoUseCaseTest {

	@Mock
	private HistoricoStatusOrdemServicoRepository historicoRepository;

	@Mock
	private BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase;

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private ClienteNotificationService clienteNotificationService;

	@InjectMocks
	private RegistrarHistoricoStatusOrdemServicoUseCase useCase;

	@Test
	void shouldRegisterHistoryAndNotifyClient() {
		UUID ordemId = UUID.randomUUID();
		UUID usuarioId = UUID.randomUUID();
		OrdemServico ordemServico = criarOrdemServico(ordemId);
		when(buscarResponsavelTransicaoUseCase.executar()).thenReturn(new ResponsavelTransicao(usuarioId, "mecanico"));
		when(historicoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(ordemServicoRepository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemServico));

		HistoricoStatusOrdemServico historico = useCase.executar(ordemId, StatusOrdemServico.AGUARDANDO_EXECUCAO,
				StatusOrdemServico.EM_EXECUCAO);

		assertNotNull(historico.getId());
		assertEquals(ordemId, historico.getIdOrdemServico());
		assertEquals(usuarioId, historico.getIdUsuario());
		assertEquals("mecanico", historico.getUsernameUsuario());
		verify(clienteNotificationService).notificarMudancaStatus(ordemServico, StatusOrdemServico.AGUARDANDO_EXECUCAO,
				StatusOrdemServico.EM_EXECUCAO);
	}

	@Test
	void shouldNotFailTransitionWhenNotificationThrows() {
		UUID ordemId = UUID.randomUUID();
		OrdemServico ordemServico = criarOrdemServico(ordemId);
		when(buscarResponsavelTransicaoUseCase.executar())
			.thenReturn(new ResponsavelTransicao(UUID.randomUUID(), "mecanico"));
		when(historicoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(ordemServicoRepository.buscarPorId(ordemId)).thenReturn(Optional.of(ordemServico));
		doThrow(new RuntimeException("falha de canal")).when(clienteNotificationService)
			.notificarMudancaStatus(any(), any(), any());

		HistoricoStatusOrdemServico historico = useCase.executar(ordemId, StatusOrdemServico.RECEBIDO,
				StatusOrdemServico.EM_DIAGNOSTICO);

		assertNotNull(historico.getId());
		verify(clienteNotificationService).notificarMudancaStatus(eq(ordemServico), any(), any());
	}

	private OrdemServico criarOrdemServico(UUID ordemId) {
		return new OrdemServico(ordemId, UUID.randomUUID(), UUID.randomUUID(), StatusOrdemServico.EM_EXECUCAO,
				List.of(), "OS-2026-00001", null, LocalDateTime.now().minusDays(1), LocalDateTime.now(), null);
	}

}
