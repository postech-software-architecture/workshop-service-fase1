package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.TempoMedioExecucaoResponse;
import com.postech.workshop_service.api.dtos.TempoMedioPorTipoServicoResponse;
import com.postech.workshop_service.application.usecases.ConsultarTempoMedioExecucaoUseCase;
import com.postech.workshop_service.application.usecases.ConsultarTempoMedioPorTipoServicoUseCase;
import com.postech.workshop_service.domain.valueobjects.TempoMedioGlobal;
import com.postech.workshop_service.domain.valueobjects.TempoMedioPorTipoServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller de metricas administrativas de tempo de execucao das ordens de servico.
 */
@RestController
@RequestMapping("/api/v1/metricas")
@Tag(name = "Metricas", description = "Monitoramento do tempo medio de execucao das ordens de servico")
@SecurityRequirement(name = "bearerAuth")
public class MetricaController {

	private final ConsultarTempoMedioExecucaoUseCase consultarTempoMedioExecucaoUseCase;

	private final ConsultarTempoMedioPorTipoServicoUseCase consultarTempoMedioPorTipoServicoUseCase;

	public MetricaController(ConsultarTempoMedioExecucaoUseCase consultarTempoMedioExecucaoUseCase,
			ConsultarTempoMedioPorTipoServicoUseCase consultarTempoMedioPorTipoServicoUseCase) {
		this.consultarTempoMedioExecucaoUseCase = consultarTempoMedioExecucaoUseCase;
		this.consultarTempoMedioPorTipoServicoUseCase = consultarTempoMedioPorTipoServicoUseCase;
	}

	@GetMapping("/tempo-medio-execucao")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Tempo medio global de execucao",
			description = "Retorna media, minimo, maximo e total de ordens de servico finalizadas ou entregues. Restrito ao perfil ADMINISTRADOR.")
	public ResponseEntity<TempoMedioExecucaoResponse> consultarTempoMedioGlobal() {
		TempoMedioGlobal resultado = consultarTempoMedioExecucaoUseCase.executar();
		TempoMedioExecucaoResponse response = TempoMedioExecucaoResponse.builder()
			.totalOrdens(resultado.totalOrdens())
			.tempoMedioExecucaoMinutos(resultado.tempoMedioMinutos())
			.tempoMinimoExecucaoMinutos(resultado.tempoMinimoMinutos())
			.tempoMaximoExecucaoMinutos(resultado.tempoMaximoMinutos())
			.build();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/tempo-medio-execucao/por-tipo-servico")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Tempo medio de execucao por tipo de servico",
			description = "Retorna o tempo medio de execucao agrupado pela descricao dos itens de servico presentes na composicao tecnica das ordens finalizadas. Restrito ao perfil ADMINISTRADOR.")
	public ResponseEntity<List<TempoMedioPorTipoServicoResponse>> consultarTempoMedioPorTipoServico() {
		List<TempoMedioPorTipoServico> resultados = consultarTempoMedioPorTipoServicoUseCase.executar();
		List<TempoMedioPorTipoServicoResponse> response = resultados.stream()
			.map(r -> TempoMedioPorTipoServicoResponse.builder()
				.descricaoServico(r.descricaoServico())
				.totalExecucoes(r.totalExecucoes())
				.tempoMedioExecucaoMinutos(r.tempoMedioMinutos())
				.build())
			.toList();
		return ResponseEntity.ok(response);
	}

}
