package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.OrdemServicoDetalheResponse;
import com.postech.workshop_service.api.dtos.OrdemServicoResponse;
import com.postech.workshop_service.api.dtos.PaginaOrdensServicoResponse;
import com.postech.workshop_service.api.dtos.StatusOrdemServicoResponse;
import com.postech.workshop_service.application.usecases.BuscarOrdemServicoPorIdUseCase;
import com.postech.workshop_service.application.usecases.ConsultarStatusOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.CriarOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.ListarMinhasOrdensServicoUseCase;
import com.postech.workshop_service.application.usecases.ListarOrdensServicoUseCase;
import com.postech.workshop_service.application.usecases.ResultadoCriacaoOrdemServico;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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

	private final BuscarOrdemServicoPorIdUseCase buscarOrdemServicoPorIdUseCase;

	private final ListarOrdensServicoUseCase listarOrdensServicoUseCase;

	private final ListarMinhasOrdensServicoUseCase listarMinhasOrdensServicoUseCase;

	private final ConsultarStatusOrdemServicoUseCase consultarStatusOrdemServicoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param criarOrdemServicoUseCase caso de uso de criacao da OS.
	 * @param buscarOrdemServicoPorIdUseCase caso de uso de busca por id.
	 * @param listarOrdensServicoUseCase caso de uso de listagem administrativa.
	 * @param listarMinhasOrdensServicoUseCase caso de uso de listagem para clientes.
	 * @param consultarStatusOrdemServicoUseCase caso de uso de consulta de status pelo
	 * cliente.
	 */
	public OrdemServicoController(CriarOrdemServicoUseCase criarOrdemServicoUseCase,
			BuscarOrdemServicoPorIdUseCase buscarOrdemServicoPorIdUseCase,
			ListarOrdensServicoUseCase listarOrdensServicoUseCase,
			ListarMinhasOrdensServicoUseCase listarMinhasOrdensServicoUseCase,
			ConsultarStatusOrdemServicoUseCase consultarStatusOrdemServicoUseCase) {
		this.criarOrdemServicoUseCase = criarOrdemServicoUseCase;
		this.buscarOrdemServicoPorIdUseCase = buscarOrdemServicoPorIdUseCase;
		this.listarOrdensServicoUseCase = listarOrdensServicoUseCase;
		this.listarMinhasOrdensServicoUseCase = listarMinhasOrdensServicoUseCase;
		this.consultarStatusOrdemServicoUseCase = consultarStatusOrdemServicoUseCase;
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

	/**
	 * Recupera o detalhe de uma ordem de servico pelo identificador.
	 * @param id identificador da OS.
	 * @return detalhe completo da OS.
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	@Operation(summary = "Buscar Ordem de Servico por id",
			description = "Retorna o detalhe completo de uma OS com itens da composicao tecnica.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "OS encontrada",
					content = @Content(schema = @Schema(implementation = OrdemServicoDetalheResponse.class))),
			@ApiResponse(responseCode = "404", description = "OS nao encontrada") })
	public ResponseEntity<OrdemServicoDetalheResponse> buscarPorId(@PathVariable UUID id) {
		OrdemServico ordem = buscarOrdemServicoPorIdUseCase.executar(id);
		return ResponseEntity.ok(OrdemServicoDetalheResponse.from(ordem));
	}

	/**
	 * Lista ordens de servico com filtros opcionais e paginacao.
	 * @param pagina pagina solicitada (zero-based).
	 * @param tamanho tamanho da pagina.
	 * @param status filtro opcional por status.
	 * @param idCliente filtro opcional por cliente.
	 * @param dataInicio data minima inclusiva de criacao.
	 * @param dataFim data maxima exclusiva de criacao.
	 * @return resposta paginada com ordens em formato resumido.
	 */
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	@Operation(summary = "Listar Ordens de Servico",
			description = "Lista ordens de servico aplicando filtros opcionais (status, cliente, intervalo de datas).")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Pagina de ordens de servico",
			content = @Content(schema = @Schema(implementation = PaginaOrdensServicoResponse.class))) })
	public ResponseEntity<PaginaOrdensServicoResponse> listar(@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanho, @RequestParam(required = false) StatusOrdemServico status,
			@RequestParam(required = false) UUID idCliente,
			@RequestParam(required = false) @DateTimeFormat(
					iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
		PaginaResultado<OrdemServico> resultado = listarOrdensServicoUseCase.executar(pagina, tamanho, status,
				idCliente, dataInicio, dataFim);
		return ResponseEntity.ok(PaginaOrdensServicoResponse.from(resultado));
	}

	/**
	 * Lista as ordens de servico do cliente autenticado.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho da pagina.
	 * @param status filtro opcional por status.
	 * @return resposta paginada das ordens do cliente autenticado.
	 */
	@GetMapping("/minhas")
	@PreAuthorize("hasRole('CLIENTE')")
	@Operation(summary = "Listar minhas Ordens de Servico",
			description = "Retorna as ordens de servico do cliente autenticado, com filtro opcional por status.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Pagina de ordens de servico do cliente",
					content = @Content(schema = @Schema(implementation = PaginaOrdensServicoResponse.class))),
			@ApiResponse(responseCode = "403", description = "Conta autenticada sem cliente vinculado") })
	public ResponseEntity<PaginaOrdensServicoResponse> listarMinhas(@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanho, @RequestParam(required = false) StatusOrdemServico status) {
		PaginaResultado<OrdemServico> resultado = listarMinhasOrdensServicoUseCase.executar(pagina, tamanho, status);
		return ResponseEntity.ok(PaginaOrdensServicoResponse.from(resultado));
	}

	/**
	 * Consulta o status atual de uma ordem de servico do cliente autenticado.
	 * @param id identificador da OS.
	 * @return status compacto da OS.
	 */
	@GetMapping("/{id}/status")
	@PreAuthorize("hasRole('CLIENTE')")
	@Operation(summary = "Consultar status da Ordem de Servico",
			description = "Permite que o cliente acompanhe o progresso da sua OS. "
					+ "Retorna 403 quando a OS pertence a outro cliente.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Status atual da OS",
					content = @Content(schema = @Schema(implementation = StatusOrdemServicoResponse.class))),
			@ApiResponse(responseCode = "403", description = "OS pertence a outro cliente"),
			@ApiResponse(responseCode = "404", description = "OS nao encontrada") })
	public ResponseEntity<StatusOrdemServicoResponse> consultarStatus(@PathVariable UUID id) {
		OrdemServico ordem = consultarStatusOrdemServicoUseCase.executar(id);
		return ResponseEntity.ok(StatusOrdemServicoResponse.from(ordem));
	}

}
