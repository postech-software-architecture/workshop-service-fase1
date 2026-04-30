package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncerrarComposicaoTecnicaUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private OrcamentoRepository orcamentoRepository;

	@Mock
	private ClienteNotificationService clienteNotificationService;

	@InjectMocks
	private EncerrarComposicaoTecnicaUseCase encerrarComposicaoTecnicaUseCase;

	@Test
	void shouldCloseCompositionWithAtLeastOneItem() {
		OrdemServico ordemServico = criarOrdemServicoComItens();
		when(ordemServicoRepository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
		when(orcamentoRepository.existePendenteAprovacaoPorOrdemServico(ordemServico.getId())).thenReturn(false);
		when(orcamentoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Orcamento orcamento = encerrarComposicaoTecnicaUseCase.executar(ordemServico.getId());

		assertEquals(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, ordemServico.getStatus());
		assertEquals(StatusOrcamento.PENDENTE_APROVACAO, orcamento.getStatus());
		verify(clienteNotificationService).notificarOrcamentoPendente(ordemServico, orcamento);
	}

	@Test
	void shouldPreventClosingCompositionWithoutItems() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		when(ordemServicoRepository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));

		assertThrows(RegraDeNegocioException.class,
				() -> encerrarComposicaoTecnicaUseCase.executar(ordemServico.getId()));
		verify(orcamentoRepository, never()).salvar(any());
		verify(clienteNotificationService, never()).notificarOrcamentoPendente(any(), any());
	}

	@Test
	void shouldGeneratePendingBudgetWhenClosingComposition() {
		OrdemServico ordemServico = criarOrdemServicoComItens();
		when(ordemServicoRepository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
		when(orcamentoRepository.existePendenteAprovacaoPorOrdemServico(ordemServico.getId())).thenReturn(false);
		when(orcamentoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Orcamento orcamento = encerrarComposicaoTecnicaUseCase.executar(ordemServico.getId());

		assertEquals(StatusOrcamento.PENDENTE_APROVACAO, orcamento.getStatus());
	}

	@Test
	void shouldCopyOrderItemsToBudget() {
		OrdemServico ordemServico = criarOrdemServicoComItens();
		when(ordemServicoRepository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
		when(orcamentoRepository.existePendenteAprovacaoPorOrdemServico(ordemServico.getId())).thenReturn(false);
		when(orcamentoRepository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

		encerrarComposicaoTecnicaUseCase.executar(ordemServico.getId());

		ArgumentCaptor<Orcamento> captor = ArgumentCaptor.forClass(Orcamento.class);
		verify(orcamentoRepository).salvar(captor.capture());
		assertEquals(ordemServico.getItensComposicao().size(), captor.getValue().getItens().size());
		assertEquals(ordemServico.getItensComposicao().get(0).getDescricao(),
				captor.getValue().getItens().get(0).getDescricao());
	}

	@Test
	void shouldPreventMoreThanOnePendingBudgetForSameOrder() {
		OrdemServico ordemServico = criarOrdemServicoComItens();
		when(ordemServicoRepository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
		when(orcamentoRepository.existePendenteAprovacaoPorOrdemServico(ordemServico.getId())).thenReturn(true);

		assertThrows(RegraDeNegocioException.class,
				() -> encerrarComposicaoTecnicaUseCase.executar(ordemServico.getId()));
		verify(orcamentoRepository, never()).salvar(any());
	}

	private OrdemServico criarOrdemServicoComItens() {
		return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				StatusOrdemServico.EM_COMPOSICAO,
				List.of(new ItemComposicaoTecnica("Troca de oleo", new BigDecimal("120.00"),
						TipoItemComposicaoTecnica.SERVICO),
						new ItemComposicaoTecnica("Filtro de oleo", new BigDecimal("45.00"),
								TipoItemComposicaoTecnica.PECA)),
				LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);
	}

}
