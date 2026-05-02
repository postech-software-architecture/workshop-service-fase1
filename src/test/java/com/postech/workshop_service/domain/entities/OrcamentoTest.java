package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.APROVADO);
		LocalDateTime atualizacaoAnterior = orcamento.getDataUltimaAtualizacao();

		RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, orcamento::enviarParaAprovacao);

		assertEquals("Nao e permitido enviar para aprovacao um orcamento com status APROVADO.", exception.getMessage());
		assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
		assertEquals(atualizacaoAnterior, orcamento.getDataUltimaAtualizacao());
	}

	@Test
	void shouldApprovePendingBudget() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE);

		orcamento.aprovar(ordemServico);

		assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
	}

	@Test
	void shouldApproveServiceOriginalAndAdvanceOrderToExecution() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE);

		orcamento.aprovar(ordemServico);

		assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.AGUARDANDO_EXECUCAO, ordemServico.getStatus());
	}

	@Test
	void shouldApproveAddedServiceBudgetWithoutChangingOrder() {
		Orcamento orcamento = criarOrcamentoAdicaoServico(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_EXECUCAO);

		orcamento.aprovar(ordemServico);

		assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.AGUARDANDO_EXECUCAO, ordemServico.getStatus());
	}

	@Test
	void shouldRejectApprovalWhenStatusIsInvalid() {
		Orcamento orcamento = criarOrcamentoServicoOriginal();
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE);

		RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
				() -> orcamento.aprovar(ordemServico));

		assertEquals("Nao e permitido aprovar um orcamento com status CRIADO.", exception.getMessage());
	}

	@Test
	void shouldRejectPendingBudget() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO);

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
	void shouldCancelCreatedBudget() {
		Orcamento orcamento = criarOrcamentoServicoOriginal();
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE);

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
	}

	@Test
	void shouldCancelPendingBudget() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE);

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
	}

	@Test
	void shouldCancelApprovedBudgetWhenOrderIsAlreadyInExecution() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.APROVADO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_EXECUCAO);

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.AGUARDANDO_EXECUCAO, ordemServico.getStatus());
	}

	@Test
	void shouldCancelServiceOriginalBudgetAndCancelOrderWhenOrderIsCancellable() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE);

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.CANCELADA, ordemServico.getStatus());
	}

	@Test
	void shouldCancelServiceOriginalBudgetWithoutCancellingOrderWhenOrderIsNotCancellable() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_EXECUCAO);

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.AGUARDANDO_EXECUCAO, ordemServico.getStatus());
	}

	@Test
	void shouldCancelAddedServiceBudgetWithoutCancellingOrder() {
		Orcamento orcamento = criarOrcamentoAdicaoServico(StatusOrcamento.PENDENTE_APROVACAO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE);

		orcamento.cancelar(ordemServico);

		assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
		assertEquals(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, ordemServico.getStatus());
	}

	@Test
	void shouldRejectCancelWhenStatusIsInvalid() {
		Orcamento orcamento = criarOrcamentoReconstituido(StatusOrcamento.REJEITADO);
		OrdemServico ordemServico = criarOrdemServicoComStatus(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE);

		RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
				() -> orcamento.cancelar(ordemServico));

		assertEquals("Nao e permitido cancelar um orcamento com status REJEITADO.", exception.getMessage());
	}

	// --- helpers ---

	private Orcamento criarOrcamentoServicoOriginal() {
		return new Orcamento(null, UUID.randomUUID(), new BigDecimal("120.00"),
				List.of(new ItemOrcamento("Troca de oleo", new BigDecimal("120.00"))), TipoOrcamento.SERVICO_ORIGINAL);
	}

	private Orcamento criarOrcamentoReconstituido(StatusOrcamento status) {
		LocalDateTime criacao = LocalDateTime.now().minusDays(2);
		LocalDateTime atualizacao = LocalDateTime.now().minusDays(1);
		return new Orcamento(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("120.00"),
				List.of(new ItemOrcamento("Troca de oleo", new BigDecimal("120.00"))), TipoOrcamento.SERVICO_ORIGINAL,
				status, criacao, atualizacao, null);
	}

	private Orcamento criarOrcamentoAdicaoServico(StatusOrcamento status) {
		LocalDateTime criacao = LocalDateTime.now().minusDays(2);
		LocalDateTime atualizacao = LocalDateTime.now().minusDays(1);
		return new Orcamento(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("120.00"),
				List.of(new ItemOrcamento("Revisao adicional", new BigDecimal("120.00"))), TipoOrcamento.ADICAO_SERVICO,
				status, criacao, atualizacao, null);
	}

	private OrdemServico criarOrdemServicoComStatus(StatusOrdemServico status) {
		LocalDateTime criacao = LocalDateTime.now().minusDays(2);
		LocalDateTime atualizacao = LocalDateTime.now().minusDays(1);
		return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), status,
				List.of(new ItemComposicaoTecnica("Troca de oleo", new BigDecimal("120.00"),
						TipoItemComposicaoTecnica.SERVICO)),
				"OS-2026-00001", null, criacao, atualizacao, null);
	}

}
