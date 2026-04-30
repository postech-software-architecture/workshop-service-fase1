package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdemServicoTest {

	@Test
	void shouldCreateOrdemServicoWithRequiredDataAndComposingStatus() {
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();

		OrdemServico ordemServico = new OrdemServico(null, clienteId, veiculoId);

		assertNotNull(ordemServico.getId());
		assertEquals(clienteId, ordemServico.getIdCliente());
		assertEquals(veiculoId, ordemServico.getIdVeiculo());
		assertEquals(StatusOrdemServico.EM_COMPOSICAO, ordemServico.getStatus());
		assertTrue(ordemServico.getItensComposicao().isEmpty());
		assertNotNull(ordemServico.getDataCriacao());
		assertEquals(ordemServico.getDataCriacao(), ordemServico.getDataUltimaAtualizacao());
	}

	@Test
	void shouldRebuildPersistedOrdemServico() {
		UUID ordemId = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		LocalDateTime criacao = LocalDateTime.now().minusDays(2);
		LocalDateTime atualizacao = LocalDateTime.now().minusDays(1);
		LocalDateTime remocao = LocalDateTime.now();
		List<ItemComposicaoTecnica> itens = List.of(new ItemComposicaoTecnica("Troca de oleo", new BigDecimal("120.00"),
				TipoItemComposicaoTecnica.SERVICO));

		OrdemServico ordemServico = new OrdemServico(ordemId, clienteId, veiculoId,
				StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, itens, criacao, atualizacao, remocao);

		assertEquals(ordemId, ordemServico.getId());
		assertEquals(clienteId, ordemServico.getIdCliente());
		assertEquals(veiculoId, ordemServico.getIdVeiculo());
		assertEquals(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, ordemServico.getStatus());
		assertEquals(itens, ordemServico.getItensComposicao());
		assertEquals(criacao, ordemServico.getDataCriacao());
		assertEquals(atualizacao, ordemServico.getDataUltimaAtualizacao());
		assertEquals(remocao, ordemServico.getDataRemocao());
	}

	@Test
	void shouldRejectInvalidIdentifiersAndStatus() {
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();

		assertThrows(IllegalArgumentException.class, () -> new OrdemServico(null, null, veiculoId));
		assertThrows(IllegalArgumentException.class, () -> new OrdemServico(null, clienteId, null));
		assertThrows(IllegalArgumentException.class,
				() -> new OrdemServico(UUID.randomUUID(), clienteId, veiculoId, null, List.of(), agora, agora, null));
		assertThrows(IllegalArgumentException.class, () -> new OrdemServico(UUID.randomUUID(), clienteId, veiculoId,
				StatusOrdemServico.EM_COMPOSICAO, Arrays.asList((ItemComposicaoTecnica) null), agora, agora, null));
	}

	@Test
	void shouldExposeIfOrderHasCompositionItems() {
		OrdemServico ordemSemItens = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		OrdemServico ordemComItens = criarOrdemServico(StatusOrdemServico.EM_COMPOSICAO,
				List.of(new ItemComposicaoTecnica("Filtro", new BigDecimal("30.00"), TipoItemComposicaoTecnica.PECA)));

		assertFalse(ordemSemItens.possuiItensComposicao());
		assertTrue(ordemComItens.possuiItensComposicao());
	}

	@Test
	void shouldCloseCompositionWhenStatusAndItemsAllowIt() {
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.EM_COMPOSICAO, List
			.of(new ItemComposicaoTecnica("Alinhamento", new BigDecimal("90.00"), TipoItemComposicaoTecnica.SERVICO)));
		LocalDateTime dataAnterior = ordemServico.getDataUltimaAtualizacao();

		ordemServico.encerrarComposicao();

		assertEquals(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, ordemServico.getStatus());
		assertTrue(ordemServico.getDataUltimaAtualizacao().isAfter(dataAnterior)
				|| ordemServico.getDataUltimaAtualizacao().isEqual(dataAnterior));
	}

	@Test
	void shouldRejectCloseCompositionWithoutItems() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());

		RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
				ordemServico::encerrarComposicao);

		assertEquals("Nao e permitido encerrar a composicao tecnica de uma ordem de servico sem itens.",
				exception.getMessage());
		assertEquals(StatusOrdemServico.EM_COMPOSICAO, ordemServico.getStatus());
	}

	@Test
	void shouldRejectCloseCompositionWhenStatusIsInvalid() {
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.CANCELADA, List
			.of(new ItemComposicaoTecnica("Fluido", new BigDecimal("15.00"), TipoItemComposicaoTecnica.INSUMO)));

		RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
				ordemServico::encerrarComposicao);

		assertEquals("Nao e permitido encerrar a composicao tecnica de uma ordem de servico com status CANCELADA.",
				exception.getMessage());
	}

	@Test
	void shouldReturnOrderToCompositionWhenStatusAllowsIt() {
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, List.of());

		ordemServico.voltarParaComposicao();

		assertEquals(StatusOrdemServico.EM_COMPOSICAO, ordemServico.getStatus());
	}

	@Test
	void shouldMarkOrderAsWaitingExecutionWhenStatusAllowsIt() {
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, List.of());

		ordemServico.marcarComoAguardandoExecucao();

		assertEquals(StatusOrdemServico.AGUARDANDO_EXECUCAO, ordemServico.getStatus());
	}

	@Test
	void shouldCancelOrderWhenStatusAllowsIt() {
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, List.of());

		ordemServico.cancelar();

		assertEquals(StatusOrdemServico.CANCELADA, ordemServico.getStatus());
	}

	@Test
	void shouldRejectStateTransitionsOutsideWaitingClientResponse() {
		OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.EM_COMPOSICAO, List.of());

		RegraDeNegocioException voltarException = assertThrows(RegraDeNegocioException.class,
				ordemServico::voltarParaComposicao);
		RegraDeNegocioException execucaoException = assertThrows(RegraDeNegocioException.class,
				ordemServico::marcarComoAguardandoExecucao);
		RegraDeNegocioException cancelamentoException = assertThrows(RegraDeNegocioException.class,
				ordemServico::cancelar);

		assertEquals("Nao e permitido voltar para composicao uma ordem de servico com status EM_COMPOSICAO.",
				voltarException.getMessage());
		assertEquals("Nao e permitido marcar como aguardando execucao uma ordem de servico com status EM_COMPOSICAO.",
				execucaoException.getMessage());
		assertEquals("Nao e permitido cancelar uma ordem de servico com status EM_COMPOSICAO.",
				cancelamentoException.getMessage());
	}

	private OrdemServico criarOrdemServico(StatusOrdemServico status, List<ItemComposicaoTecnica> itens) {
		LocalDateTime criacao = LocalDateTime.now().minusDays(2);
		LocalDateTime atualizacao = LocalDateTime.now().minusDays(1);
		return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), status, itens, criacao,
				atualizacao, null);
	}

}
