package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IniciarExecucaoUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private EstoqueRepository estoqueRepository;

	@Mock
	private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	@Mock
	private RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	@InjectMocks
	private IniciarExecucaoUseCase useCase;

	@Test
	void shouldStartExecutionAndRegisterHistory() {
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.AGUARDANDO_EXECUCAO);
		Estoque estoque = new Estoque(UUID.randomUUID(), UUID.randomUUID(), "Prateleira A", new BigDecimal("4"), true,
				0, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));
		MovimentacaoEstoque reserva = new MovimentacaoEstoque(UUID.randomUUID(), estoque.getId(),
				TipoMovimentacao.RESERVA, BigDecimal.ONE, new BigDecimal("4"), new BigDecimal("3"),
				"Reserva para OS " + ordemServico.getNumero(), ordemServico.getId(), UUID.randomUUID());
		when(ordemServicoRepository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
		when(ordemServicoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(movimentacaoEstoqueRepository.listarPorOrdemServico(ordemServico.getId())).thenReturn(List.of(reserva));
		when(estoqueRepository.buscarPorId(estoque.getId(), true)).thenReturn(Optional.of(estoque));
		when(movimentacaoEstoqueRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		OrdemServico resultado = useCase.executar(ordemServico.getId());

		assertEquals(StatusOrdemServico.EM_EXECUCAO, resultado.getStatus());
		assertNotNull(resultado.getDataInicioExecucao());
		verify(movimentacaoEstoqueRepository).listarPorOrdemServico(ordemServico.getId());
		verify(estoqueRepository).buscarPorId(estoque.getId(), true);
		verify(movimentacaoEstoqueRepository).salvar(any(MovimentacaoEstoque.class));
		verify(registrarHistoricoUseCase).executar(resultado.getId(), StatusOrdemServico.AGUARDANDO_EXECUCAO,
				StatusOrdemServico.EM_EXECUCAO);
	}

	@Test
	void shouldReturnNotFoundWhenOrderDoesNotExist() {
		UUID id = UUID.randomUUID();
		when(ordemServicoRepository.buscarPorId(id)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(id));
	}

	private OrdemServico criarOrdemServico(StatusOrdemServico status) {
		return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), status, List.of(),
				"OS-2026-00001", null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

}
