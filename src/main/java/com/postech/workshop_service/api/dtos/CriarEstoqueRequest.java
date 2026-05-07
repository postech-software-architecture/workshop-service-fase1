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
@Schema(description = "Dados para criacao de um novo estoque")
public class CriarEstoqueRequest {

	@NotNull(message = "O identificador da peca e obrigatorio")
	@Schema(description = "ID da peca", example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID pecaInsumoId;

	@NotBlank(message = "A localizacao e obrigatoria")
	@Size(max = 100, message = "A localizacao deve ter no maximo 100 caracteres")
	@Schema(example = "Prateleira A2", description = "Localizacao fisica do estoque")
	private String localizacao;

	@NotNull(message = "A quantidade inicial e obrigatoria")
	@PositiveOrZero(message = "A quantidade nao pode ser negativa")
	@Schema(example = "10", description = "Quantidade inicial em estoque")
	private BigDecimal quantidade;

}
