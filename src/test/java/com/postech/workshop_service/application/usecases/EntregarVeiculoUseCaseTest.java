package com.postech.workshop_service.application.usecases;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntregarVeiculoUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	@InjectMocks
	private EntregarVeiculoUseCase useCase;

	@Test
	void shouldDeliverVehicleAndRegisterHistory() {
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.FINALIZADA);
		when(ordemServicoRepository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
		when(ordemServicoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		OrdemServico resultado = useCase.executar(ordemServico.getId());

		assertEquals(StatusOrdemServico.ENTREGUE, resultado.getStatus());
		assertNotNull(resultado.getDataEntrega());
		verify(registrarHistoricoUseCase).executar(resultado.getId(), StatusOrdemServico.FINALIZADA,
				StatusOrdemServico.ENTREGUE);
	}

	private OrdemServico criarOrdemServico(StatusOrdemServico status) {
		return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), status, List.of(),
				"OS-2026-00001", null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

}
