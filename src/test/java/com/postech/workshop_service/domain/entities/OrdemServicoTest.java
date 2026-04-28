package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdemServicoTest {

	@Test
	void shouldCreateOrdemServicoWithRequiredDataAndReceivedStatus() {
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();

		OrdemServico ordemServico = new OrdemServico(null, clienteId, veiculoId);

		assertNotNull(ordemServico.getId());
		assertEquals(clienteId, ordemServico.getIdCliente());
		assertEquals(veiculoId, ordemServico.getIdVeiculo());
		assertEquals(StatusOrdemServico.RECEBIDA, ordemServico.getStatus());
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

		OrdemServico ordemServico = new OrdemServico(ordemId, clienteId, veiculoId,
				StatusOrdemServico.AGUARDANDO_APROVACAO_ORCAMENTO, criacao, atualizacao, remocao);

		assertEquals(ordemId, ordemServico.getId());
		assertEquals(clienteId, ordemServico.getIdCliente());
		assertEquals(veiculoId, ordemServico.getIdVeiculo());
		assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO_ORCAMENTO, ordemServico.getStatus());
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
				() -> new OrdemServico(UUID.randomUUID(), clienteId, veiculoId, null, agora, agora, null));
	}

	@Test
	void shouldReturnTrueForCancelableStatuses() {
		UUID ordemId = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();

		OrdemServico recebida = new OrdemServico(ordemId, clienteId, veiculoId, StatusOrdemServico.RECEBIDA, agora,
				agora, null);
		OrdemServico aguardandoOrcamento = new OrdemServico(ordemId, clienteId, veiculoId,
				StatusOrdemServico.AGUARDANDO_APROVACAO_ORCAMENTO, agora, agora, null);

		assertTrue(recebida.podeSerCancelada());
		assertTrue(aguardandoOrcamento.podeSerCancelada());
	}

	@Test
	void shouldReturnFalseForNonCancelableStatuses() {
		UUID ordemId = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();

		OrdemServico emExecucao = new OrdemServico(ordemId, clienteId, veiculoId, StatusOrdemServico.EM_EXECUCAO, agora,
				agora, null);
		OrdemServico finalizada = new OrdemServico(ordemId, clienteId, veiculoId, StatusOrdemServico.FINALIZADA, agora,
				agora, null);
		OrdemServico cancelada = new OrdemServico(ordemId, clienteId, veiculoId, StatusOrdemServico.CANCELADA, agora,
				agora, null);

		assertFalse(emExecucao.podeSerCancelada());
		assertFalse(finalizada.podeSerCancelada());
		assertFalse(cancelada.podeSerCancelada());
	}

	@Test
	void shouldCancelOrdemServicoWhenStatusAllowsIt() {
		OrdemServico ordemServico = new OrdemServico(null, UUID.randomUUID(), UUID.randomUUID());
		LocalDateTime dataAnterior = ordemServico.getDataUltimaAtualizacao();

		ordemServico.cancelar();

		assertEquals(StatusOrdemServico.CANCELADA, ordemServico.getStatus());
		assertTrue(ordemServico.getDataUltimaAtualizacao().isAfter(dataAnterior)
				|| ordemServico.getDataUltimaAtualizacao().isEqual(dataAnterior));
	}

	@Test
	void shouldRejectCancelWhenStatusDoesNotAllowIt() {
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		OrdemServico ordemServico = new OrdemServico(UUID.randomUUID(), clienteId, veiculoId,
				StatusOrdemServico.EM_EXECUCAO, agora, agora, null);

		RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, ordemServico::cancelar);

		assertEquals("Nao e permitido cancelar uma ordem de servico com status EM_EXECUCAO.", exception.getMessage());
		assertEquals(StatusOrdemServico.EM_EXECUCAO, ordemServico.getStatus());
		assertEquals(agora, ordemServico.getDataUltimaAtualizacao());
	}

}
