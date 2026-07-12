package com.postech.workshop_service.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusOrdemServicoTest {

	@Test
	void shouldOrderQueuePrioritiesFromMostToLeastUrgent() {
		assertTrue(StatusOrdemServico.EM_EXECUCAO.prioridadeFila() < StatusOrdemServico.AGUARDANDO_APROVACAO
			.prioridadeFila());
		assertTrue(StatusOrdemServico.AGUARDANDO_APROVACAO.prioridadeFila() < StatusOrdemServico.EM_DIAGNOSTICO
			.prioridadeFila());
		assertTrue(StatusOrdemServico.EM_DIAGNOSTICO.prioridadeFila() < StatusOrdemServico.RECEBIDO.prioridadeFila());
	}

	@Test
	void shouldGiveMaxPriorityToNonQueuedStatuses() {
		assertEquals(Integer.MAX_VALUE, StatusOrdemServico.EM_COMPOSICAO.prioridadeFila());
		assertEquals(Integer.MAX_VALUE, StatusOrdemServico.AGUARDANDO_EXECUCAO.prioridadeFila());
		assertEquals(Integer.MAX_VALUE, StatusOrdemServico.CANCELADA.prioridadeFila());
	}

	@Test
	void shouldListClosedStatuses() {
		assertTrue(StatusOrdemServico.ENCERRADOS.contains(StatusOrdemServico.FINALIZADA));
		assertTrue(StatusOrdemServico.ENCERRADOS.contains(StatusOrdemServico.ENTREGUE));
		assertTrue(StatusOrdemServico.ENCERRADOS.contains(StatusOrdemServico.CANCELADA));
		assertFalse(StatusOrdemServico.ENCERRADOS.contains(StatusOrdemServico.EM_EXECUCAO));
		assertEquals(3, StatusOrdemServico.ENCERRADOS.size());
	}

}
