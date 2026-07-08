package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiberarReservasEstoqueServiceTest {

	@Mock
	private EstoqueRepository estoqueRepository;

	@Mock
	private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	@InjectMocks
	private LiberarReservasEstoqueService service;

	@Test
	void shouldReleaseReservationWhenNoStockWithdrawalHappened() {
		OrdemServico ordemServico = criarOrdemServico();
		UUID estoqueId = UUID.randomUUID();
		Estoque estoque = new Estoque(estoqueId, UUID.randomUUID(), "Prateleira A", new BigDecimal("10"));
		MovimentacaoEstoque reserva = criarMovimentacao(estoqueId, TipoMovimentacao.RESERVA, ordemServico);

		when(movimentacaoEstoqueRepository.listarPorOrdemServico(ordemServico.getId())).thenReturn(List.of(reserva));
		when(estoqueRepository.buscarPorId(estoqueId, true)).thenReturn(Optional.of(estoque));

		service.executar(ordemServico, "motivo");

		verify(estoqueRepository).salvar(estoque);
		verify(movimentacaoEstoqueRepository).salvar(any());
	}

	@Test
	void shouldSkipReleaseWhenStockAlreadyWithdrawn() {
		OrdemServico ordemServico = criarOrdemServico();
		MovimentacaoEstoque saida = criarMovimentacao(UUID.randomUUID(), TipoMovimentacao.SAIDA, ordemServico);

		when(movimentacaoEstoqueRepository.listarPorOrdemServico(ordemServico.getId())).thenReturn(List.of(saida));

		service.executar(ordemServico, "motivo");

		verify(estoqueRepository, never()).salvar(any());
		verify(movimentacaoEstoqueRepository, never()).salvar(any());
	}

	@Test
	void shouldIgnoreReservationWhenStockNotFound() {
		OrdemServico ordemServico = criarOrdemServico();
		UUID estoqueId = UUID.randomUUID();
		MovimentacaoEstoque reserva = criarMovimentacao(estoqueId, TipoMovimentacao.RESERVA, ordemServico);

		when(movimentacaoEstoqueRepository.listarPorOrdemServico(ordemServico.getId())).thenReturn(List.of(reserva));
		when(estoqueRepository.buscarPorId(estoqueId, true)).thenReturn(Optional.empty());

		service.executar(ordemServico, "motivo");

		verify(estoqueRepository, never()).salvar(any());
		verify(movimentacaoEstoqueRepository, never()).salvar(any());
	}

	private OrdemServico criarOrdemServico() {
		return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				StatusOrdemServico.AGUARDANDO_APROVACAO, List.of(), "OS-2026-00001", null,
				LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

	private MovimentacaoEstoque criarMovimentacao(UUID estoqueId, TipoMovimentacao tipo, OrdemServico ordemServico) {
		return new MovimentacaoEstoque(UUID.randomUUID(), estoqueId, tipo, new BigDecimal("2"), new BigDecimal("10"),
				new BigDecimal("8"), "Reserva para OS " + ordemServico.getNumero(), ordemServico.getId(),
				UUID.randomUUID());
	}

}
