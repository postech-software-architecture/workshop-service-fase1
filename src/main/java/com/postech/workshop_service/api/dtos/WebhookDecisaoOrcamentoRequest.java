package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload recebido do sistema externo com a decisao de aprovacao/recusa de um orcamento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Decisao externa (aprovacao ou recusa) de um orcamento")
public class WebhookDecisaoOrcamentoRequest {

	@NotNull(message = "A decisao e obrigatoria")
	@Schema(description = "Decisao do cliente: APROVADO ou RECUSADO", example = "APROVADO")
	private Decisao decisao;

	@Schema(description = "Origem/canal do sistema externo", example = "portal-cliente")
	private String origem;

	@Schema(description = "Observacao livre sobre a decisao", example = "Aprovado pelo cliente via app")
	private String observacao;

	@Schema(description = "Identificador unico do evento (para rastreabilidade/idempotencia)",
			example = "e2b1c0a4-1234-4a2b-9c8d-0f1e2d3c4b5a")
	private String idEvento;

	/**
	 * Decisao possivel do cliente sobre o orcamento.
	 */
	public enum Decisao {

		APROVADO, RECUSADO

	}

}
