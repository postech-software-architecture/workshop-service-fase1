package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.ItemOrcamento;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.TipoOrcamento;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
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
class RejeitarOrcamentoUseCaseTest {

	@Mock
	private OrcamentoRepository orcamentoRepository;

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private EstoqueRepository estoqueRepository;

	@Mock
	private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	@Mock
	private PecaInsumoRepository pecaInsumoRepository;

	@Mock
	private MecanicoNotificationService mecanicoNotificationService;

	@InjectMocks
	private RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase;

	@Test
	void shouldRejectPendingBudget() {
		Orcamento orcamento = criarOrcamento(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE,
				orcamento.getIdOrdemServico());
		when(orcamentoRepository.buscarPorId(orcamento.getId())).thenReturn(Optional.of(orcamento));
		when(ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())).thenReturn(Optional.of(ordemServico));
		when(orcamentoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Orcamento resultado = rejeitarOrcamentoUseCase.executar(orcamento.getId());

		assertEquals(StatusOrcamento.REJEITADO, resultado.getStatus());
		assertEquals(StatusOrdemServico.EM_COMPOSICAO, ordemServico.getStatus());
		verify(mecanicoNotificationService).notificarAtualizacaoOrcamento(ordemServico, resultado);
	}

	@Test
	void shouldReleaseStockReservationOnRejection() {
		UUID pecaId = UUID.randomUUID();
		PecaInsumo peca = new PecaInsumo(pecaId, "REF-001", "Oleo 5W30", new BigDecimal("50.00"), BigDecimal.ZERO,
				UnidadeMedida.L, TipoItem.INSUMO);
		ItemComposicaoTecnica itemPeca = new ItemComposicaoTecnica("Oleo 5W30", new BigDecimal("100.00"),
				TipoItemComposicaoTecnica.PECA, pecaId);
		Estoque estoque = new Estoque(UUID.randomUUID(), pecaId, "Prateleira A", BigDecimal.ZERO);

		Orcamento orcamento = criarOrcamento(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = new OrdemServico(orcamento.getIdOrdemServico(), UUID.randomUUID(),
				UUID.randomUUID(), StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, List.of(itemPeca), "OS-2026-00001",
				null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);

		when(orcamentoRepository.buscarPorId(orcamento.getId())).thenReturn(Optional.of(orcamento));
		when(ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())).thenReturn(Optional.of(ordemServico));
		when(pecaInsumoRepository.buscarPorId(pecaId, true)).thenReturn(Optional.of(peca));
		when(estoqueRepository.listarPorPeca(pecaId, false)).thenReturn(List.of(estoque));
		when(orcamentoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		rejeitarOrcamentoUseCase.executar(orcamento.getId());

		verify(estoqueRepository).listarPorPeca(pecaId, false);
		verify(movimentacaoEstoqueRepository).salvar(any());
	}

	@Test
	void shouldPreventRejectionIfOrderIsNotWaitingClientResponse() {
		Orcamento orcamento = criarOrcamento(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.CANCELADA, orcamento.getIdOrdemServico());
		when(orcamentoRepository.buscarPorId(orcamento.getId())).thenReturn(Optional.of(orcamento));
		when(ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())).thenReturn(Optional.of(ordemServico));

		assertThrows(RegraDeNegocioException.class, () -> rejeitarOrcamentoUseCase.executar(orcamento.getId()));
	}

	private Orcamento criarOrcamento(StatusOrcamento status) {
		return new Orcamento(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("90.00"),
				List.of(new ItemOrcamento("Alinhamento", new BigDecimal("90.00"))), TipoOrcamento.SERVICO_ORIGINAL,
				status, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

	private OrdemServico criarOrdemServico(StatusOrdemServico status, UUID idOrdemServico) {
		return new OrdemServico(idOrdemServico, UUID.randomUUID(), UUID.randomUUID(), status, List.of(), null, null,
				LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

}
