package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para registrar uma movimentacao de estoque")
public class MovimentacaoRequest {

	@NotNull(message = "O identificador do estoque e obrigatorio")
	@Schema(description = "ID do estoque", example = "550e8400-e29b-41d4-a716-446655440001")
	private UUID estoqueId;

	@NotBlank(message = "O tipo de movimentacao e obrigatorio")
	@Schema(example = "ENTRADA", description = "Tipo: ENTRADA ou SAIDA")
	private String tipo;

	@NotNull(message = "A quantidade e obrigatoria")
	@Positive(message = "A quantidade deve ser maior que zero")
	@Schema(example = "5", description = "Quantidade movimentada")
	private BigDecimal quantidade;

	@Size(max = 500, message = "O motivo deve ter no maximo 500 caracteres")
	@Schema(example = "Compra de reposicao", description = "Motivo da movimentacao")
	private String motivo;

}
