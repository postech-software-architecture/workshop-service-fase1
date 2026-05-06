package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Representacao completa de um orcamento retornado pela API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos do orcamento")
public class OrcamentoResponse {

	@Schema(description = "Identificador unico do orcamento", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID id;

	@Schema(description = "Identificador da ordem de servico vinculada",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID idOrdemServico;

	@Schema(description = "Valor total calculado", example = "310.00")
	private BigDecimal valorTotal;

	@Schema(description = "Status atual do orcamento", example = "APROVADO")
	private String status;

	@Schema(description = "Tipo do orcamento", example = "SERVICO_ORIGINAL")
	private String tipo;

	@Schema(description = "Itens que compoe o orcamento")
	private List<ItemOrcamentoResponse> itens;

	@Schema(description = "Data e hora de criacao do orcamento")
	private LocalDateTime dataCriacao;

	@Schema(description = "Data e hora da ultima atualizacao")
	private LocalDateTime dataUltimaAtualizacao;

	/**
	 * Representacao de um item dentro do orcamento.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Item cobrado no orcamento")
	public static class ItemOrcamentoResponse {

		@Schema(description = "Descricao do servico ou peca", example = "Troca de oleo")
		private String descricao;

		@Schema(description = "Valor unitario do item", example = "100.00")
		private BigDecimal valor;

	}

}