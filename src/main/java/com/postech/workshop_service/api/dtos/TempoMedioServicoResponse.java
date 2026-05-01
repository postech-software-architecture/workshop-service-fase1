package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Resposta contendo informacoes de tempo estimado e medio de execucao do servico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informações de tempo estimado e médio de execução do serviço")
public class TempoMedioServicoResponse {

	@Schema(example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID servicoId;

	@Schema(example = "60")
	private int tempoEstimadoMinutos;

	@Schema(example = "null", nullable = true, description = "Tempo medio real em minutos; null quando nao disponivel")
	private Integer tempoMedioRealMinutos;

	@Schema(example = "Disponível após implementação de Ordens de Serviço (Issue #5)")
	private String disponivelAPartirDe;

}
