package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.ItemOrcamento;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.TipoOrcamento;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarOrcamentoUseCaseTest {

	@Mock
	private OrcamentoRepository orcamentoRepository;

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private EstoqueRepository estoqueRepository;

	@Mock
	private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	@Mock
	private MecanicoNotificationService mecanicoNotificationService;

	@Mock
	private RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	@InjectMocks
	private CancelarOrcamentoUseCase cancelarOrcamentoUseCase;

	@Test
	void shouldCancelPendingBudget() {
		Orcamento orcamento = criarOrcamento(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE,
				orcamento.getIdOrdemServico());
		when(orcamentoRepository.buscarPorId(orcamento.getId())).thenReturn(Optional.of(orcamento));
		when(ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())).thenReturn(Optional.of(ordemServico));
		when(orcamentoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Orcamento resultado = cancelarOrcamentoUseCase.executar(orcamento.getId());

		assertEquals(StatusOrcamento.CANCELADO, resultado.getStatus());
		assertEquals(StatusOrdemServico.CANCELADA, ordemServico.getStatus());
		verify(mecanicoNotificationService).notificarAtualizacaoOrcamento(ordemServico, resultado);
	}

	@Test
	void shouldReleaseStockReservationOnCancellation() {
		UUID pecaId = UUID.randomUUID();
		ItemComposicaoTecnica itemPeca = new ItemComposicaoTecnica("Filtro de ar", new BigDecimal("60.00"),
				TipoItemComposicaoTecnica.PECA, pecaId);
		UUID estoqueId = UUID.randomUUID();
		Estoque estoque = new Estoque(estoqueId, pecaId, "Prateleira B", new BigDecimal("5"));
		BigDecimal quantidadeReservada = new BigDecimal("2");

		Orcamento orcamento = criarOrcamento(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = new OrdemServico(orcamento.getIdOrdemServico(), UUID.randomUUID(),
				UUID.randomUUID(), StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, List.of(itemPeca), "OS-2026-00002",
				null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
		MovimentacaoEstoque reservaOriginal = new MovimentacaoEstoque(UUID.randomUUID(), estoqueId,
				TipoMovimentacao.RESERVA, quantidadeReservada, new BigDecimal("5"), new BigDecimal("3"),
				"Reserva para OS " + ordemServico.getNumero(), ordemServico.getId(), orcamento.getId());

		when(orcamentoRepository.buscarPorId(orcamento.getId())).thenReturn(Optional.of(orcamento));
		when(ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())).thenReturn(Optional.of(ordemServico));
		when(movimentacaoEstoqueRepository.listarPorOrdemServico(ordemServico.getId()))
			.thenReturn(List.of(reservaOriginal));
		when(estoqueRepository.buscarPorId(estoqueId, true)).thenReturn(Optional.of(estoque));
		when(orcamentoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		cancelarOrcamentoUseCase.executar(orcamento.getId());

		verify(movimentacaoEstoqueRepository).listarPorOrdemServico(ordemServico.getId());
		verify(estoqueRepository).buscarPorId(estoqueId, true);
		verify(estoqueRepository).salvar(estoque);
		verify(movimentacaoEstoqueRepository).salvar(any());
	}

	@Test
	void shouldPreventCancellationIfOrderIsNotWaitingClientResponse() {
		Orcamento orcamento = criarOrcamento(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.AGUARDANDO_EXECUCAO,
				orcamento.getIdOrdemServico());
		when(orcamentoRepository.buscarPorId(orcamento.getId())).thenReturn(Optional.of(orcamento));
		when(ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())).thenReturn(Optional.of(ordemServico));

		assertThrows(RegraDeNegocioException.class, () -> cancelarOrcamentoUseCase.executar(orcamento.getId()));
	}

	private Orcamento criarOrcamento(StatusOrcamento status) {
		return new Orcamento(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("180.00"),
				List.of(new ItemOrcamento("Kit correia", new BigDecimal("180.00"))), TipoOrcamento.SERVICO_ORIGINAL,
				status, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

	private OrdemServico criarOrdemServico(StatusOrdemServico status, UUID idOrdemServico) {
		return new OrdemServico(idOrdemServico, UUID.randomUUID(), UUID.randomUUID(), status, List.of(), null, null,
				LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

}
