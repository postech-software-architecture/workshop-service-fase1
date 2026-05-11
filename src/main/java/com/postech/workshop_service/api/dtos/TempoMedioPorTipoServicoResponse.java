package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta com o tempo medio de execucao agrupado por tipo de servico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tempo medio de execucao agrupado pela descricao do servico na composicao tecnica")
public class TempoMedioPorTipoServicoResponse {

	@Schema(description = "Descricao do item de servico na composicao tecnica", example = "Troca de oleo e filtro")
	private String descricaoServico;

	@Schema(description = "Numero de ordens finalizadas que continham este servico", example = "8")
	private long totalExecucoes;

	@Schema(description = "Tempo medio de execucao em minutos para ordens com este servico", example = "45.0")
	private double tempoMedioExecucaoMinutos;

}
