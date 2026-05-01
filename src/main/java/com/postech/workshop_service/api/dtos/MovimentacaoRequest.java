package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para registrar uma movimentacao de estoque")
public class MovimentacaoRequest {

	@NotNull(message = "O identificador do estoque e obrigatorio")
	@Schema(example = "550e8400-e29b-41d4-a716-446655440001", description = "ID do estoque")
	private java.util.UUID estoqueId;

	@NotBlank(message = "O tipo de movimentacao e obrigatorio")
	@Schema(example = "ENTRADA", description = "Tipo: ENTRADA, SAIDA ou AJUSTE")
	private String tipo;

	@NotNull(message = "A quantidade e obrigatoria")
	@PositiveOrZero(message = "A quantidade nao pode ser negativa")
	@Schema(example = "5", description = "Quantidade movimentada (valor absoluto para AJUSTE)")
	private BigDecimal quantidade;

	@Size(max = 500, message = "O motivo deve ter no maximo 500 caracteres")
	@Schema(example = "Compra de reposicao", description = "Motivo da movimentacao (obrigatorio para AJUSTE)")
	private String motivo;

}
