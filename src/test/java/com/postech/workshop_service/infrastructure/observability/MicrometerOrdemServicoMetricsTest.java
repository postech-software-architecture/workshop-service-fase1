package com.postech.workshop_service.infrastructure.observability;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerOrdemServicoMetricsTest {

	private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

	private final MicrometerOrdemServicoMetrics metrics = new MicrometerOrdemServicoMetrics(registry);

	private final UUID orderId = UUID.randomUUID();

	private final UUID userId = UUID.randomUUID();

	@Test
	void recordsCreatedOnlyWithContractualTags() {
		metrics.ordemServicoCriada();

		assertThat(registry.get("workshop.ordem_servico.created.count").counter().count()).isEqualTo(1);
		assertThat(registry.get("workshop.ordem_servico.created.count").counter().getId().getTags())
			.extracting(tag -> tag.getKey())
			.containsExactlyInAnyOrder("operation", "outcome", "environment");
	}

	@Test
	void recordsStageDurationInSecondsFromPersistedHistory() {
		HistoricoStatusOrdemServico entered = history(StatusOrdemServico.RECEBIDO, StatusOrdemServico.EM_DIAGNOSTICO,
				LocalDateTime.of(2026, 9, 13, 10, 0));
		HistoricoStatusOrdemServico exited = history(StatusOrdemServico.EM_DIAGNOSTICO,
				StatusOrdemServico.EM_COMPOSICAO, LocalDateTime.of(2026, 9, 13, 10, 2, 30));

		metrics.transicaoRegistrada(exited, List.of(entered, exited));

		assertThat(registry.get("workshop.ordem_servico.status.duration")
			.timer()
			.totalTime(java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(150.0);
		assertThat(registry.get("workshop.ordem_servico.status.duration").timer().getId().getTag("stage"))
			.isEqualTo("diagnostico");
	}

	@Test
	void ignoresTransitionWithoutAStageContract() {
		HistoricoStatusOrdemServico transition = history(StatusOrdemServico.RECEBIDO, StatusOrdemServico.EM_COMPOSICAO,
				LocalDateTime.of(2026, 9, 13, 10, 0));

		metrics.transicaoRegistrada(transition, List.of(transition));

		assertThat(registry.find("workshop.ordem_servico.status.duration").timer()).isNull();
	}

	@Test
	void recordsProcessingErrorEvenWhenTransactionRollsBack() {
		TransactionSynchronizationManager.initSynchronization();
		try {
			metrics.erroDeProcessamento("execucao", "finalize");

			assertThat(registry.get("workshop.ordem_servico.processing.error.count").counter().count()).isEqualTo(1);
			assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
		}
		finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	void defersSuccessMetricUntilCommit() {
		TransactionSynchronizationManager.initSynchronization();
		try {
			metrics.ordemServicoCriada();
			assertThat(registry.find("workshop.ordem_servico.created.count").counter()).isNull();

			for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
				synchronization.afterCommit();
			}
			assertThat(registry.get("workshop.ordem_servico.created.count").counter().count()).isEqualTo(1);
		}
		finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	private HistoricoStatusOrdemServico history(StatusOrdemServico previous, StatusOrdemServico next,
			LocalDateTime at) {
		return new HistoricoStatusOrdemServico(UUID.randomUUID(), orderId, previous, next, at, userId, "tester");
	}

}
