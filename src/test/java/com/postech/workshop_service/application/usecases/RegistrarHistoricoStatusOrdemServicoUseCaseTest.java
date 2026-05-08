package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarHistoricoStatusOrdemServicoUseCaseTest {

	@Mock
	private HistoricoStatusOrdemServicoRepository historicoRepository;

	@Mock
	private BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase;

	@InjectMocks
	private RegistrarHistoricoStatusOrdemServicoUseCase useCase;

	@Test
	void shouldRegisterHistoryWithAuthenticatedResponsible() {
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
	}

}
