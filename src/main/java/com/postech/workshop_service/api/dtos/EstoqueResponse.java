package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de resposta de um estoque")
public class EstoqueResponse {

	@Schema(example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID id;

	@Schema(example = "550e8400-e29b-41d4-a716-446655440001")
	private UUID pecaInsumoId;

	@Schema(example = "Prateleira A2")
	private String localizacao;

	@Schema(example = "10.500")
	private BigDecimal quantidade;

	@Schema(example = "true")
	private boolean ativo;

	@Schema(example = "5")
	private int versao;

	@Schema(example = "2026-04-29T22:00:00")
	private LocalDateTime dataCriacao;

	@Schema(example = "2026-04-29T22:30:00")
	private LocalDateTime dataUltimaAtualizacao;

}
