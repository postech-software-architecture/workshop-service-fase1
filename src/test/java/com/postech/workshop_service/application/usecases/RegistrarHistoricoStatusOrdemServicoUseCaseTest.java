package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarHistoricoStatusOrdemServicoUseCaseTest {

	@Mock
	private HistoricoStatusOrdemServicoRepository historicoRepository;

	@Mock
	private BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private RegistrarHistoricoStatusOrdemServicoUseCase useCase;

	@Test
	void shouldRegisterHistoryAndPublishStatusChangeEvent() {
		UUID ordemId = UUID.randomUUID();
		UUID usuarioId = UUID.randomUUID();
		when(buscarResponsavelTransicaoUseCase.executar()).thenReturn(new ResponsavelTransicao(usuarioId, "mecanico"));
		when(historicoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		HistoricoStatusOrdemServico historico = useCase.executar(ordemId, StatusOrdemServico.AGUARDANDO_EXECUCAO,
				StatusOrdemServico.EM_EXECUCAO);

		assertNotNull(historico.getId());
		assertEquals(ordemId, historico.getIdOrdemServico());
		assertEquals(usuarioId, historico.getIdUsuario());
		assertEquals("mecanico", historico.getUsernameUsuario());

		ArgumentCaptor<MudancaStatusOrdemServicoEvent> captor = ArgumentCaptor
			.forClass(MudancaStatusOrdemServicoEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		MudancaStatusOrdemServicoEvent evento = captor.getValue();
		assertEquals(ordemId, evento.idOrdemServico());
		assertEquals(StatusOrdemServico.AGUARDANDO_EXECUCAO, evento.anterior());
		assertEquals(StatusOrdemServico.EM_EXECUCAO, evento.novo());
	}

}
