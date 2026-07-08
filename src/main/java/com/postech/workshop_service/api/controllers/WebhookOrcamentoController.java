package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.controllers.support.WebhookTokenValidator;
import com.postech.workshop_service.api.dtos.OrcamentoResponse;
import com.postech.workshop_service.api.dtos.OrcamentoResponse.ItemOrcamentoResponse;
import com.postech.workshop_service.api.dtos.WebhookDecisaoOrcamentoRequest;
import com.postech.workshop_service.application.usecases.AprovarOrcamentoUseCase;
import com.postech.workshop_service.application.usecases.RejeitarOrcamentoUseCase;
import com.postech.workshop_service.domain.entities.Orcamento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Webhook de integracao maquina-a-maquina que recebe a decisao (aprovacao ou recusa) de
 * um orcamento vinda de um sistema externo, autenticado por token de servico.
 *
 * <p>
 * A recusa (RECUSADO) delega ao caso de uso de rejeicao, que mantem a OS viva e a retorna
 * a composicao, permitindo um novo orcamento. Idempotencia: uma reentrega do mesmo evento
 * encontra o orcamento em estado ja alterado e resulta em 422 (nunca aplica o efeito duas
 * vezes).
 * </p>
 */
@RestController
@RequestMapping("/api/v1/webhooks/orcamentos")
@Tag(name = "Webhooks", description = "Integracao maquina-a-maquina de decisao de orcamento")
public class WebhookOrcamentoController {

	private final WebhookTokenValidator tokenValidator;

	private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;

	private final RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param tokenValidator validador do token de servico do webhook.
	 * @param aprovarOrcamentoUseCase caso de uso de aprovacao de orcamento.
	 * @param rejeitarOrcamentoUseCase caso de uso de rejeicao de orcamento.
	 */
	public WebhookOrcamentoController(WebhookTokenValidator tokenValidator,
			AprovarOrcamentoUseCase aprovarOrcamentoUseCase, RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase) {
		this.tokenValidator = tokenValidator;
		this.aprovarOrcamentoUseCase = aprovarOrcamentoUseCase;
		this.rejeitarOrcamentoUseCase = rejeitarOrcamentoUseCase;
	}

	/**
	 * Recebe a decisao externa de um orcamento e delega ao caso de uso correspondente.
	 * @param id identificador do orcamento.
	 * @param token token de servico recebido no header {@code X-Webhook-Token}.
	 * @param request payload com a decisao (APROVADO ou RECUSADO).
	 * @return o orcamento resultante, ou 401 quando o token e ausente/invalido.
	 */
	@PostMapping("/{id}/decisao")
	@Operation(summary = "Receber decisao externa de orcamento",
			description = "Autenticado por X-Webhook-Token. APROVADO aprova o orcamento; RECUSADO delega a rejeicao "
					+ "(mantem a OS viva e a retorna a composicao).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Decisao aplicada",
					content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente ou invalido"),
			@ApiResponse(responseCode = "404", description = "Orcamento nao encontrado"),
			@ApiResponse(responseCode = "422", description = "Orcamento nao esta pendente / OS em estado invalido") })
	public ResponseEntity<OrcamentoResponse> receberDecisao(@PathVariable UUID id,
			@RequestHeader(value = "X-Webhook-Token", required = false) String token,
			@Valid @RequestBody WebhookDecisaoOrcamentoRequest request) {
		if (!tokenValidator.valido(token)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		Orcamento orcamento = switch (request.getDecisao()) {
			case APROVADO -> aprovarOrcamentoUseCase.executar(id);
			case RECUSADO -> rejeitarOrcamentoUseCase.executar(id);
		};
		return ResponseEntity.ok(toResponse(orcamento));
	}

	private OrcamentoResponse toResponse(Orcamento orcamento) {
		return OrcamentoResponse.builder()
			.id(orcamento.getId())
			.idOrdemServico(orcamento.getIdOrdemServico())
			.valorTotal(orcamento.getValor())
			.status(orcamento.getStatus().name())
			.tipo(orcamento.getTipo().name())
			.itens(orcamento.getItens()
				.stream()
				.map(item -> ItemOrcamentoResponse.builder()
					.descricao(item.getDescricao())
					.valor(item.getValor())
					.build())
				.toList())
			.dataCriacao(orcamento.getDataCriacao())
			.dataUltimaAtualizacao(orcamento.getDataUltimaAtualizacao())
			.build();
	}

}
