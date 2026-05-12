package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload para adicionar um item (servico ou peca) a composicao tecnica da OS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para adicionar um item a composicao tecnica da OS")
public class AdicionarItemOrdemServicoRequest {

	@NotNull(message = "O tipo do item e obrigatorio")
	@Schema(description = "Tipo do item: SERVICO ou PECA", example = "SERVICO")
	private TipoItem tipo;

	@Schema(description = "Identificador do servico do catalogo (obrigatorio para SERVICO)")
	private UUID servicoId;

	@Schema(description = "Identificador da peca do catalogo (obrigatorio para PECA)")
	private UUID pecaId;

	@Positive(message = "A quantidade deve ser maior que zero")
	@Schema(description = "Quantidade de unidades do item (default 1 para servicos)", example = "1")
	private BigDecimal quantidade;

	/**
	 * Tipo de item aceito no payload.
	 */
	public enum TipoItem {

		SERVICO, PECA

	}

}
