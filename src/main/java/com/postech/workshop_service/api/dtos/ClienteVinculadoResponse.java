package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Resumo do cliente vinculado a um veiculo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cliente vinculado ao veículo")
public class ClienteVinculadoResponse {

	@Schema(example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID id;

	@Schema(example = "João da Silva")
	private String nome;

	@Schema(example = "***.654.321-**")
	private String documentoMascarado;

}
