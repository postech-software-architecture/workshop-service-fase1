package com.postech.workshop_service.infrastructure.observability;

import com.postech.workshop_service.application.usecases.OrdemServicoMetrics;
import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Adapter Micrometer para as métricas de negócio da W5. */
@Component
public class MicrometerOrdemServicoMetrics implements OrdemServicoMetrics {

	static final String CREATED = "workshop.ordem_servico.created.count";

	static final String STATUS_DURATION = "workshop.ordem_servico.status.duration";

	static final String PROCESSING_ERROR = "workshop.ordem_servico.processing.error.count";

	static final String INTEGRATION_ERROR = "workshop.integration.error.count";

	private final MeterRegistry registry;

	private final String environment;

	public MicrometerOrdemServicoMetrics(MeterRegistry registry) {
		this.registry = registry;
		this.environment = System.getenv().getOrDefault("DEPLOYMENT_ENVIRONMENT", "unknown");
	}

	@Override
	public void ordemServicoCriada() {
		afterCommit(() -> counter(CREATED, "operation", "create", "outcome", "success").increment());
	}

	@Override
	public void transicaoRegistrada(HistoricoStatusOrdemServico historico,
			List<HistoricoStatusOrdemServico> historicoCompleto) {
		String stage = stageFor(historico.getStatusAnterior(), historico.getStatusNovo());
		if (stage == null) {
			return;
		}
		List<HistoricoStatusOrdemServico> entries = new ArrayList<>(historicoCompleto);
		if (entries.stream().noneMatch(entry -> entry.getId().equals(historico.getId()))) {
			entries.add(historico);
		}
		HistoricoStatusOrdemServico start = entries.stream()
			.filter(entry -> entry.getStatusNovo() == startStatus(stage))
			.filter(entry -> entry.getDataTransicao().isBefore(historico.getDataTransicao()))
			.max((left, right) -> left.getDataTransicao().compareTo(right.getDataTransicao()))
			.orElse(null);
		if (start == null) {
			return;
		}
		long seconds = Math.max(0,
				Duration.between(start.getDataTransicao(), historico.getDataTransicao()).toSeconds());
		afterCommit(() -> Timer.builder(STATUS_DURATION)
			.description("Tempo entre a entrada e a saída de uma etapa da OS")
			.tags("stage", stage, "status", historico.getStatusNovo().name(), "environment", environment)
			.register(registry)
			.record(Duration.ofSeconds(seconds)));
	}

	@Override
	public void erroDeProcessamento(String stage, String operation) {
		afterCommit(() -> counter(PROCESSING_ERROR, "stage", stage, "operation", operation, "outcome", "error")
			.increment());
	}

	@Override
	public void erroDeIntegracao(String integration, String operation) {
		afterCommit(
				() -> counter(INTEGRATION_ERROR, "integration", integration, "operation", operation, "outcome", "error")
					.increment());
	}

	private io.micrometer.core.instrument.Counter counter(String name, String... tags) {
		String[] completeTags = new String[tags.length + 2];
		System.arraycopy(tags, 0, completeTags, 0, tags.length);
		completeTags[tags.length] = "environment";
		completeTags[tags.length + 1] = environment;
		return registry.counter(name, completeTags);
	}

	private String stageFor(StatusOrdemServico previous, StatusOrdemServico next) {
		if (previous == StatusOrdemServico.EM_DIAGNOSTICO) {
			return "diagnostico";
		}
		if (previous == StatusOrdemServico.EM_EXECUCAO && next == StatusOrdemServico.FINALIZADA) {
			return "execucao";
		}
		if (previous == StatusOrdemServico.FINALIZADA && next == StatusOrdemServico.ENTREGUE) {
			return "finalizacao";
		}
		return null;
	}

	private StatusOrdemServico startStatus(String stage) {
		return switch (stage) {
			case "diagnostico" -> StatusOrdemServico.EM_DIAGNOSTICO;
			case "execucao" -> StatusOrdemServico.EM_EXECUCAO;
			case "finalizacao" -> StatusOrdemServico.FINALIZADA;
			default -> throw new IllegalArgumentException("Etapa desconhecida: " + stage);
		};
	}

	private void afterCommit(Runnable action) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			action.run();
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				action.run();
			}
		});
	}

}
