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
@Schema(description = "Dados de resposta de uma movimentacao de estoque")
public class MovimentacaoResponse {

	@Schema(example = "550e8400-e29b-41d4-a716-446655440002")
	private UUID id;

	@Schema(example = "550e8400-e29b-41d4-a716-446655440001")
	private UUID estoqueId;

	@Schema(example = "550e8400-e29b-41d4-a716-446655440010")
	private UUID ordemServicoId;

	@Schema(example = "550e8400-e29b-41d4-a716-446655440011")
	private UUID orcamentoId;

	@Schema(example = "ENTRADA")
	private String tipo;

	@Schema(example = "5.000")
	private BigDecimal quantidade;

	@Schema(example = "10.000")
	private BigDecimal quantidadeAnterior;

	@Schema(example = "15.000")
	private BigDecimal quantidadePosterior;

	@Schema(example = "Compra de reposicao")
	private String motivo;

	@Schema(example = "2026-04-29T22:00:00")
	private LocalDateTime dataMovimentacao;

}
