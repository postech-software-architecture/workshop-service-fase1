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
@Schema(description = "Dados de resposta de uma peca ou insumo")
public class PecaResponse {

	@Schema(example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID id;

	@Schema(example = "FIL-001")
	private String sku;

	@Schema(example = "Filtro de oleo 5W30")
	private String nome;

	@Schema(example = "45.90")
	private BigDecimal valorUnitario;

	@Schema(example = "5")
	private BigDecimal estoqueMinimo;

	@Schema(example = "UN")
	private String unidadeMedida;

	@Schema(example = "PECA")
	private String tipoItem;

	@Schema(example = "Bosch")
	private String fornecedor;

	@Schema(example = "7891234567890")
	private String codigoBarras;

	@Schema(example = "Bosch")
	private String marca;

	@Schema(example = "Filtros")
	private String categoria;

	@Schema(example = "Hatch compacto ate 2.0")
	private String aplicacao;

	@Schema(example = "Compativel com Gol, Fox e Polo")
	private String observacoes;

	@Schema(example = "true")
	private boolean ativo;

	@Schema(example = "12.500")
	private BigDecimal quantidadeTotal;

	@Schema(example = "10")
	private int versao;

	@Schema(example = "2026-04-29T22:00:00")
	private LocalDateTime dataCriacao;

	@Schema(example = "2026-04-29T22:30:00")
	private LocalDateTime dataUltimaAtualizacao;

	@Schema(example = "null")
	private LocalDateTime dataRemocao;

}
