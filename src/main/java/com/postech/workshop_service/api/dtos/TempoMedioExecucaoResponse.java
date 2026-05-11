package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta com o tempo medio global de execucao das ordens de servico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tempo medio global de execucao das ordens de servico finalizadas")
public class TempoMedioExecucaoResponse {

	@Schema(description = "Total de ordens finalizadas ou entregues consideradas no calculo", example = "42")
	private long totalOrdens;

	@Schema(description = "Tempo medio de execucao em minutos", example = "145.5")
	private double tempoMedioExecucaoMinutos;

	@Schema(description = "Menor tempo de execucao registrado em minutos", example = "30.0")
	private double tempoMinimoExecucaoMinutos;

	@Schema(description = "Maior tempo de execucao registrado em minutos", example = "480.0")
	private double tempoMaximoExecucaoMinutos;

}
