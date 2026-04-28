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

class OrcamentoTest {

	@Test
	void shouldCreateOrcamentoWithRequiredDataAndCreatedStatus() {
		UUID ordemServicoId = UUID.randomUUID();
		ItemOrcamento item = new ItemOrcamento(" Troca de oleo ", new BigDecimal("120.00"));

		Orcamento orcamento = new Orcamento(null, ordemServicoId, new BigDecimal("120.00"), List.of(item),
				TipoOrcamento.SERVICO_ORIGINAL);

		assertNotNull(orcamento.getId());
		assertEquals(ordemServicoId, orcamento.getIdOrdemServico());
		assertEquals(new BigDecimal("120.00"), orcamento.getValor());
		assertEquals(List.of(item), orcamento.getItens());
		assertEquals(TipoOrcamento.SERVICO_ORIGINAL, orcamento.getTipo());
		assertEquals(StatusOrcamento.CRIADO, orcamento.getStatus());
		assertNotNull(orcamento.getDataCriacao());
		assertEquals(orcamento.getDataCriacao(), orcamento.getDataUltimaAtualizacao());
	}

	@Test
	void shouldRebuildPersistedOrcamento() {
		UUID orcamentoId = UUID.randomUUID();
		UUID ordemServicoId = UUID.randomUUID();
		LocalDateTime criacao = LocalDateTime.now().minusDays(2);
		LocalDateTime atualizacao = LocalDateTime.now().minusDays(1);
		LocalDateTime remocao = LocalDateTime.now();
		ItemOrcamento item = new ItemOrcamento("Alinhamento", new BigDecimal("80.00"));

		Orcamento orcamento = new Orcamento(orcamentoId, ordemServicoId, new BigDecimal("80.00"), List.of(item),
				TipoOrcamento.ADICAO_SERVICO, StatusOrcamento.PENDENTE_APROVACAO, criacao, atualizacao, remocao);

		assertEquals(orcamentoId, orcamento.getId());
		assertEquals(ordemServicoId, orcamento.getIdOrdemServico());
		assertEquals(new BigDecimal("80.00"), orcamento.getValor());
		assertEquals(List.of(item), orcamento.getItens());
		assertEquals(TipoOrcamento.ADICAO_SERVICO, orcamento.getTipo());
		assertEquals(StatusOrcamento.PENDENTE_APROVACAO, orcamento.getStatus());
		assertEquals(criacao, orcamento.getDataCriacao());
		assertEquals(atualizacao, orcamento.getDataUltimaAtualizacao());
		assertEquals(remocao, orcamento.getDataRemocao());
	}

	@Test
	void shouldCreateItemOrcamentoWithSanitizedDescription() {
		ItemOrcamento item = new ItemOrcamento("  Pastilha   de freio  ", new BigDecimal("50.00"));

		assertEquals("Pastilha de freio", item.getDescricao());
		assertEquals(new BigDecimal("50.00"), item.getValor());
	}

	@Test
	void shouldRejectInvalidCreationData() {
		ItemOrcamento item = new ItemOrcamento("Balanceamento", new BigDecimal("35.00"));
		LocalDateTime agora = LocalDateTime.now();

		assertThrows(IllegalArgumentException.class, () -> new ItemOrcamento("   ", new BigDecimal("10.00")));
		assertThrows(IllegalArgumentException.class, () -> new ItemOrcamento("Servico", null));
		assertThrows(IllegalArgumentException.class, () -> new Orcamento(null, null, new BigDecimal("35.00"),
				List.of(item), TipoOrcamento.SERVICO_ORIGINAL));
		assertThrows(IllegalArgumentException.class,
				() -> new Orcamento(null, UUID.randomUUID(), null, List.of(item), TipoOrcamento.SERVICO_ORIGINAL));
		assertThrows(IllegalArgumentException.class, () -> new Orcamento(null, UUID.randomUUID(),
				new BigDecimal("35.00"), List.of(), TipoOrcamento.SERVICO_ORIGINAL));
		assertThrows(IllegalArgumentException.class, () -> new Orcamento(null, UUID.randomUUID(),
				new BigDecimal("35.00"), Arrays.asList(item, null), TipoOrcamento.SERVICO_ORIGINAL));
		assertThrows(IllegalArgumentException.class, () -> new Orcamento(UUID.randomUUID(), UUID.randomUUID(),
				new BigDecimal("35.00"), List.of(item), TipoOrcamento.SERVICO_ORIGINAL, null, agora, agora, null));
	}

	@Test
	void shouldSendCreatedOrcamentoForApproval() {
		Orcamento orcamento = criarOrcamentoServicoOriginal();
		LocalDateTime atualizacaoAnterior = orcamento.getDataUltimaAtualizacao();

		orcamento.enviarParaAprovacao();

		assertEquals(StatusOrcamento.PENDENTE_APROVACAO, orcamento.getStatus());
		assertTrue(orcamento.getDataUltimaAtualizacao().isAfter(atualizacaoAnterior)
				|| orcamento.getDataUltimaAtualizacao().isEqual(atualizacaoAnterior));
	}

	@Test
	void shouldRejectSendForApprovalWhenStatusIsInvalid() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.APROVADO, TipoOrcamento.SERVICO_ORIGINAL);
		LocalDateTime atualizacaoAnterior = orcamento.getDataUltimaAtualizacao();

		RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, orcamento::enviarParaAprovacao);

		assertEquals("Nao e permitido enviar para aprovacao um orcamento com status APROVADO.", exception.getMessage());
		assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
		assertEquals(atualizacaoAnterior, orcamento.getDataUltimaAtualizacao());
	}

	@Test
	void shouldApproveOriginalBudgetAndStartOrderExecution() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO,
				TipoOrcamento.SERVICO_ORIGINAL, ordemServico.getId());

		orcamento.aprovar(ordemServico);

		assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.EM_EXECUCAO, ordemServico.getStatus());
		assertFalse(ordemServico.podeSerCancelada());
	}

	@Test
	void shouldApproveAdditionalBudgetWithoutChangingOrderStatus() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO,
				TipoOrcamento.ADICAO_SERVICO, ordemServico.getId());

		orcamento.aprovar(ordemServico);

		assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.RECEBIDA, ordemServico.getStatus());
	}

	@Test
	void shouldRejectApprovalWhenStatusIsInvalidOrOrderDoesNotMatch() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		Orcamento orcamentoCriado = criarOrcamentoServicoOriginal(ordemServico.getId());
		Orcamento orcamentoPendente = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO,
				TipoOrcamento.SERVICO_ORIGINAL);

		RegraDeNegocioException statusException = assertThrows(RegraDeNegocioException.class,
				() -> orcamentoCriado.aprovar(ordemServico));
		IllegalArgumentException vinculoException = assertThrows(IllegalArgumentException.class,
				() -> orcamentoPendente.aprovar(ordemServico));

		assertEquals("Nao e permitido aprovar um orcamento com status CRIADO.", statusException.getMessage());
		assertEquals("A ordem de servico informada nao corresponde ao orcamento.", vinculoException.getMessage());
	}

	@Test
	void shouldRejectPendingBudget() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO,
				TipoOrcamento.SERVICO_ORIGINAL, ordemServico.getId());

		orcamento.rejeitar();

		assertEquals(StatusOrcamento.REJEITADO, orcamento.getStatus());
	}

	@Test
	void shouldRejectRejectionWhenStatusIsInvalid() {
		Orcamento orcamento = criarOrcamentoServicoOriginal();
		LocalDateTime atualizacaoAnterior = orcamento.getDataUltimaAtualizacao();

		RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, orcamento::rejeitar);

		assertEquals("Nao e permitido rejeitar um orcamento com status CRIADO.", exception.getMessage());
		assertEquals(StatusOrcamento.CRIADO, orcamento.getStatus());
		assertEquals(atualizacaoAnterior, orcamento.getDataUltimaAtualizacao());
	}

	@Test
	void shouldCancelOriginalBudgetAndOrderWhenBothAllowIt() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		Orcamento orcamento = criarOrcamentoServicoOriginal(ordemServico.getId());

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.CANCELADA, ordemServico.getStatus());
	}

	@Test
	void shouldCancelAdditionalBudgetWithoutChangingOrder() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.APROVADO, TipoOrcamento.ADICAO_SERVICO,
				ordemServico.getId());

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.RECEBIDA, ordemServico.getStatus());
	}

	@Test
	void shouldCancelOriginalBudgetWithoutChangingOrderWhenOrderIsNotCancelable() {
		UUID ordemServicoId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		OrdemServico ordemServico = new OrdemServico(ordemServicoId, UUID.randomUUID(), UUID.randomUUID(),
				StatusOrdemServico.EM_EXECUCAO, agora, agora, null);
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.APROVADO, TipoOrcamento.SERVICO_ORIGINAL,
				ordemServicoId);

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.EM_EXECUCAO, ordemServico.getStatus());
	}

	@Test
	void shouldRejectCancelWhenStatusIsInvalidOrOrderDoesNotMatch() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.REJEITADO, TipoOrcamento.SERVICO_ORIGINAL,
				ordemServico.getId());
		Orcamento orcamentoPendente = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO,
				TipoOrcamento.SERVICO_ORIGINAL);

		RegraDeNegocioException statusException = assertThrows(RegraDeNegocioException.class,
				() -> orcamento.cancelar(ordemServico));
		IllegalArgumentException vinculoException = assertThrows(IllegalArgumentException.class,
				() -> orcamentoPendente.cancelar(ordemServico));

		assertEquals("Nao e permitido cancelar um orcamento com status REJEITADO.", statusException.getMessage());
		assertEquals("A ordem de servico informada nao corresponde ao orcamento.", vinculoException.getMessage());
	}

	private Orcamento criarOrcamentoServicoOriginal() {
		return criarOrcamentoServicoOriginal(UUID.randomUUID());
	}

	private Orcamento criarOrcamentoServicoOriginal(UUID ordemServicoId) {
		return new Orcamento(null, ordemServicoId, new BigDecimal("120.00"),
				List.of(new ItemOrcamento("Troca de oleo", new BigDecimal("120.00"))), TipoOrcamento.SERVICO_ORIGINAL);
	}

	private Orcamento criarOrcamentoReconstituido(StatusOrcamento status, TipoOrcamento tipo) {
		return criarOrcamentoReconstituido(status, tipo, UUID.randomUUID());
	}

	private Orcamento criarOrcamentoReconstituido(StatusOrcamento status, TipoOrcamento tipo, UUID ordemServicoId) {
		LocalDateTime criacao = LocalDateTime.now().minusDays(2);
		LocalDateTime atualizacao = LocalDateTime.now().minusDays(1);
		return new Orcamento(UUID.randomUUID(), ordemServicoId, new BigDecimal("120.00"),
				List.of(new ItemOrcamento("Troca de oleo", new BigDecimal("120.00"))), tipo, status, criacao,
				atualizacao, null);
	}

}
