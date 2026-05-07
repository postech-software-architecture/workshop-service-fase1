package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.OrdemServicoResponse;
import com.postech.workshop_service.application.usecases.CriarOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.ResultadoCriacaoOrdemServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsavel pelas operacoes de Ordens de Servico.
 */
@RestController
@RequestMapping("/api/v1/ordens-servico")
@Tag(name = "Ordens de Servico", description = "Gerenciamento do ciclo de vida das ordens de servico da oficina")
@SecurityRequirement(name = "bearerAuth")
public class OrdemServicoController {

	private final CriarOrdemServicoUseCase criarOrdemServicoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param criarOrdemServicoUseCase caso de uso de criacao da OS.
	 */
	public OrdemServicoController(CriarOrdemServicoUseCase criarOrdemServicoUseCase) {
		this.criarOrdemServicoUseCase = criarOrdemServicoUseCase;
	}

	/**
	 * Abre uma nova Ordem de Servico na recepcao do veiculo.
	 * @param request dados da recepcao.
	 * @return OS criada com orcamento pendente de aprovacao.
	 */
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	@Operation(summary = "Abrir Ordem de Servico",
			description = "Registra a recepcao do veiculo, identifica o cliente, "
					+ "calcula o orcamento automaticamente e envia para aprovacao.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "OS criada com sucesso",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados de entrada invalidos"),
			@ApiResponse(responseCode = "404", description = "Cliente nao encontrado"),
			@ApiResponse(responseCode = "422",
					description = "Regra de negocio violada (veiculo de outro cliente, estoque insuficiente, etc.)") })
	public ResponseEntity<OrdemServicoResponse> criar(@RequestBody @Valid CriarOrdemServicoRequest request) {
		ResultadoCriacaoOrdemServico resultado = criarOrdemServicoUseCase.executar(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(OrdemServicoResponse.from(resultado));
	}

}
