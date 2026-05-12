package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.OrcamentoResponse;
import com.postech.workshop_service.api.dtos.OrcamentoResponse.ItemOrcamentoResponse;
import com.postech.workshop_service.application.usecases.AprovarOrcamentoUseCase;
import com.postech.workshop_service.application.usecases.CancelarOrcamentoUseCase;
import com.postech.workshop_service.application.usecases.ListarOrcamentosPorOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.RejeitarOrcamentoUseCase;
import com.postech.workshop_service.domain.entities.Orcamento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsavel pelas operacoes de aprovacao e rejeicao de orcamentos.
 */
@RestController
@RequestMapping("/api/v1/orcamentos")
@Tag(name = "Orcamentos", description = "Gerenciamento de orcamentos vinculados a ordens de servico")
@SecurityRequirement(name = "bearerAuth")
public class OrcamentoController {

	private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;

	private final RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase;

	private final CancelarOrcamentoUseCase cancelarOrcamentoUseCase;

	private final ListarOrcamentosPorOrdemServicoUseCase listarOrcamentosPorOrdemServicoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param aprovarOrcamentoUseCase caso de uso de aprovacao.
	 * @param rejeitarOrcamentoUseCase caso de uso de rejeicao.
	 * @param cancelarOrcamentoUseCase caso de uso de cancelamento.
	 * @param listarOrcamentosPorOrdemServicoUseCase caso de uso de consulta por ordem.
	 */
	public OrcamentoController(AprovarOrcamentoUseCase aprovarOrcamentoUseCase,
			RejeitarOrcamentoUseCase rejeitarOrcamentoUseCase, CancelarOrcamentoUseCase cancelarOrcamentoUseCase,
			ListarOrcamentosPorOrdemServicoUseCase listarOrcamentosPorOrdemServicoUseCase) {
		this.aprovarOrcamentoUseCase = aprovarOrcamentoUseCase;
		this.rejeitarOrcamentoUseCase = rejeitarOrcamentoUseCase;
		this.cancelarOrcamentoUseCase = cancelarOrcamentoUseCase;
		this.listarOrcamentosPorOrdemServicoUseCase = listarOrcamentosPorOrdemServicoUseCase;
	}

	/**
	 * Lista os orcamentos vinculados a uma ordem de servico.
	 * @param idOrdemServico identificador da ordem de servico.
	 * @return orcamentos vinculados a ordem.
	 */
	@GetMapping("/ordem-servico/{idOrdemServico}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'CLIENTE')")
	@Operation(summary = "Listar orcamentos por ordem de servico",
			description = "Retorna os orcamentos vinculados a uma ordem de servico existente.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Orcamentos vinculados a ordem",
					content = @Content(
							array = @ArraySchema(schema = @Schema(implementation = OrcamentoResponse.class)))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada") })
	public ResponseEntity<List<OrcamentoResponse>> listarPorOrdemServico(@PathVariable UUID idOrdemServico) {
		List<OrcamentoResponse> orcamentos = listarOrcamentosPorOrdemServicoUseCase.executar(idOrdemServico)
			.stream()
			.map(this::toResponse)
			.toList();
		return ResponseEntity.ok(orcamentos);
	}

	/**
	 * Registra a aprovacao do cliente para um orcamento pendente.
	 * @param id identificador do orcamento.
	 * @return orcamento com status APROVADO.
	 */
	@PatchMapping("/{id}/aprovar")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'CLIENTE')")
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
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'CLIENTE')")
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

	/**
	 * Cancela um orcamento pendente, encerra a OS vinculada e libera reservas de estoque.
	 * @param id identificador do orcamento.
	 * @return orcamento com status CANCELADO.
	 */
	@PatchMapping("/{id}/cancelar")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'CLIENTE')")
	@Operation(summary = "Cancelar orcamento",
			description = "Cancela um orcamento pendente, encerra a OS vinculada e libera as reservas de estoque.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Orcamento cancelado com sucesso",
					content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Orcamento ou ordem de servico nao encontrados"),
			@ApiResponse(responseCode = "422",
					description = "Orcamento nao pode ser cancelado no estado atual da OS") })
	public ResponseEntity<OrcamentoResponse> cancelar(@PathVariable UUID id) {
		Orcamento orcamento = cancelarOrcamentoUseCase.executar(id);
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
