package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.HistoricoStatusOrdemServicoResponse;
import com.postech.workshop_service.api.dtos.OrdemServicoResponse;
import com.postech.workshop_service.application.usecases.ConsultarHistoricoOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.CriarOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.EntregarVeiculoUseCase;
import com.postech.workshop_service.application.usecases.FinalizarExecucaoUseCase;
import com.postech.workshop_service.application.usecases.IniciarExecucaoUseCase;
import com.postech.workshop_service.application.usecases.ResultadoCriacaoOrdemServico;
import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.OrdemServico;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsavel pelas operacoes de Ordens de Servico.
 */
@RestController
@RequestMapping("/api/v1/ordens-servico")
@Tag(name = "Ordens de Servico", description = "Gerenciamento do ciclo de vida das ordens de servico da oficina")
@SecurityRequirement(name = "bearerAuth")
public class OrdemServicoController {

	private final CriarOrdemServicoUseCase criarOrdemServicoUseCase;

	private final IniciarExecucaoUseCase iniciarExecucaoUseCase;

	private final FinalizarExecucaoUseCase finalizarExecucaoUseCase;

	private final EntregarVeiculoUseCase entregarVeiculoUseCase;

	private final ConsultarHistoricoOrdemServicoUseCase consultarHistoricoOrdemServicoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param criarOrdemServicoUseCase caso de uso de criacao da OS.
	 */
	public OrdemServicoController(CriarOrdemServicoUseCase criarOrdemServicoUseCase,
			IniciarExecucaoUseCase iniciarExecucaoUseCase, FinalizarExecucaoUseCase finalizarExecucaoUseCase,
			EntregarVeiculoUseCase entregarVeiculoUseCase,
			ConsultarHistoricoOrdemServicoUseCase consultarHistoricoOrdemServicoUseCase) {
		this.criarOrdemServicoUseCase = criarOrdemServicoUseCase;
		this.iniciarExecucaoUseCase = iniciarExecucaoUseCase;
		this.finalizarExecucaoUseCase = finalizarExecucaoUseCase;
		this.entregarVeiculoUseCase = entregarVeiculoUseCase;
		this.consultarHistoricoOrdemServicoUseCase = consultarHistoricoOrdemServicoUseCase;
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

	@PatchMapping("/{id}/iniciar-execucao")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MECANICO')")
	@Operation(summary = "Iniciar execucao da OS",
			description = "Avanca uma ordem aprovada para execucao tecnica e registra historico.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Execucao iniciada com sucesso",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada"),
			@ApiResponse(responseCode = "422", description = "OS nao esta aguardando execucao") })
	public ResponseEntity<OrdemServicoResponse> iniciarExecucao(@PathVariable UUID id) {
		OrdemServico ordemServico = iniciarExecucaoUseCase.executar(id);
		return ResponseEntity.ok(OrdemServicoResponse.from(ordemServico));
	}

	@PatchMapping("/{id}/finalizar-execucao")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MECANICO')")
	@Operation(summary = "Finalizar execucao da OS",
			description = "Finaliza a execucao tecnica de uma ordem em andamento e registra historico.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Execucao finalizada com sucesso",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada"),
			@ApiResponse(responseCode = "422", description = "OS nao esta em execucao") })
	public ResponseEntity<OrdemServicoResponse> finalizarExecucao(@PathVariable UUID id) {
		OrdemServico ordemServico = finalizarExecucaoUseCase.executar(id);
		return ResponseEntity.ok(OrdemServicoResponse.from(ordemServico));
	}

	@PatchMapping("/{id}/entregar")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	@Operation(summary = "Registrar entrega do veiculo",
			description = "Registra a entrega do veiculo ao cliente apos finalizacao tecnica.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Entrega registrada com sucesso",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada"),
			@ApiResponse(responseCode = "422", description = "OS nao esta finalizada") })
	public ResponseEntity<OrdemServicoResponse> entregar(@PathVariable UUID id) {
		OrdemServico ordemServico = entregarVeiculoUseCase.executar(id);
		return ResponseEntity.ok(OrdemServicoResponse.from(ordemServico));
	}

	@GetMapping("/{id}/historico-status")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MECANICO', 'ATENDENTE')")
	@Operation(summary = "Consultar historico de status da OS",
			description = "Retorna a linha do tempo de transicoes de status da ordem de servico.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Historico consultado com sucesso", content = @Content(
					schema = @Schema(implementation = HistoricoStatusOrdemServicoResponse.class, type = "array"))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada") })
	public ResponseEntity<List<HistoricoStatusOrdemServicoResponse>> consultarHistorico(@PathVariable UUID id) {
		List<HistoricoStatusOrdemServico> historico = consultarHistoricoOrdemServicoUseCase.executar(id);
		return ResponseEntity.ok(historico.stream().map(HistoricoStatusOrdemServicoResponse::from).toList());
	}

}
