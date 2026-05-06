package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.OrcamentoResponse;
import com.postech.workshop_service.api.dtos.OrcamentoResponse.ItemOrcamentoResponse;
import com.postech.workshop_service.application.usecases.AprovarOrcamentoUseCase;
import com.postech.workshop_service.application.usecases.RejeitarOrcamentoUseCase;
import com.postech.workshop_service.domain.entities.Orcamento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller responsavel pelas operacoes de aprovacao e rejeicao de orcamentos.
 */
@RestController
@RequestMapping("/api/v1/orcamentos")
@Tag(name = "Orcamentos", description = "Gerenciamento de orcamentos vinculados a ordens de servico")
public class OrcamentoController {

	private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;

	private final RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param aprovarOrcamentoUseCase caso de uso de aprovacao.
	 * @param rejeitarOrcamentoUseCase caso de uso de rejeicao.
	 */
	public OrcamentoController(AprovarOrcamentoUseCase aprovarOrcamentoUseCase,
			RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase) {
		this.aprovarOrcamentoUseCase = aprovarOrcamentoUseCase;
		this.rejeitarOrcamentoUseCase = rejeitarOrcamentoUseCase;
	}

	/**
	 * Registra a aprovacao do cliente para um orcamento pendente.
	 * @param id identificador do orcamento.
	 * @return orcamento com status APROVADO.
	 */
	@PatchMapping("/{id}/aprovar")
	@Operation(summary = "Aprovar orcamento",
			description = "Registra a aprovacao do cliente para o orcamento pendente, "
					+ "avancando a OS para aguardando execucao.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Orcamento aprovado com sucesso",
					content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Orcamento nao encontrado"),
			@ApiResponse(responseCode = "422",
					description = "Orcamento nao esta pendente de aprovacao ou OS em estado invalido") })
	public ResponseEntity<OrcamentoResponse> aprovar(@PathVariable UUID id) {
		Orcamento orcamento = aprovarOrcamentoUseCase.executar(id);
		return ResponseEntity.ok(toResponse(orcamento));
	}

	/**
	 * Registra a rejeicao do cliente para um orcamento pendente.
	 * @param id identificador do orcamento.
	 * @return orcamento com status REJEITADO.
	 */
	@PatchMapping("/{id}/rejeitar")
	@Operation(summary = "Rejeitar orcamento",
			description = "Registra a rejeicao do cliente para o orcamento pendente, "
					+ "retornando a OS para composicao.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Orcamento rejeitado com sucesso",
					content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Orcamento nao encontrado"),
			@ApiResponse(responseCode = "422",
					description = "Orcamento nao esta pendente de aprovacao ou OS em estado invalido") })
	public ResponseEntity<OrcamentoResponse> rejeitar(@PathVariable UUID id) {
		Orcamento orcamento = rejeitarOrcamentoUseCase.executar(id);
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
