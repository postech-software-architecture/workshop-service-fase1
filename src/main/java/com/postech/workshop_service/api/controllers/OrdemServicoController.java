package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.AdicionarItemOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.CriarOrdemServicoComItensRequest;
import com.postech.workshop_service.api.dtos.HistoricoStatusOrdemServicoResponse;
import com.postech.workshop_service.api.dtos.OrdemServicoDetalheResponse;
import com.postech.workshop_service.api.dtos.OrdemServicoResponse;
import com.postech.workshop_service.api.dtos.PaginaOrdensServicoResponse;
import com.postech.workshop_service.api.dtos.StatusOrdemServicoResponse;
import com.postech.workshop_service.application.usecases.AdicionarItemOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.BuscarOrdemServicoPorIdUseCase;
import com.postech.workshop_service.application.usecases.ConsultarHistoricoOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.ConsultarStatusOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.CriarOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.CriarOrdemServicoComItensUseCase;
import com.postech.workshop_service.application.usecases.DadosCriacaoOrdemServico;
import com.postech.workshop_service.application.usecases.DadosCriacaoOrdemServicoComItens;
import com.postech.workshop_service.application.usecases.EncerrarComposicaoTecnicaUseCase;
import com.postech.workshop_service.application.usecases.EncerrarDiagnosticoUseCase;
import com.postech.workshop_service.application.usecases.EntregarVeiculoUseCase;
import com.postech.workshop_service.application.usecases.FinalizarExecucaoUseCase;
import com.postech.workshop_service.application.usecases.FinalizarServicoOrdemUseCase;
import com.postech.workshop_service.application.usecases.IniciarDiagnosticoUseCase;
import com.postech.workshop_service.application.usecases.IniciarExecucaoUseCase;
import com.postech.workshop_service.application.usecases.IniciarServicoOrdemUseCase;
import com.postech.workshop_service.application.usecases.ItemPecaSolicitada;
import com.postech.workshop_service.application.usecases.ItemServicoSolicitado;
import com.postech.workshop_service.application.usecases.ListarMinhasOrdensServicoUseCase;
import com.postech.workshop_service.application.usecases.ListarOrdensServicoUseCase;
import com.postech.workshop_service.application.usecases.ResultadoCriacaoOrdemServico;
import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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

	private final CriarOrdemServicoComItensUseCase criarOrdemServicoComItensUseCase;

	private final BuscarOrdemServicoPorIdUseCase buscarOrdemServicoPorIdUseCase;

	private final ListarOrdensServicoUseCase listarOrdensServicoUseCase;

	private final ListarMinhasOrdensServicoUseCase listarMinhasOrdensServicoUseCase;

	private final ConsultarStatusOrdemServicoUseCase consultarStatusOrdemServicoUseCase;

	private final IniciarExecucaoUseCase iniciarExecucaoUseCase;

	private final FinalizarExecucaoUseCase finalizarExecucaoUseCase;

	private final EntregarVeiculoUseCase entregarVeiculoUseCase;

	private final ConsultarHistoricoOrdemServicoUseCase consultarHistoricoOrdemServicoUseCase;

	private final IniciarServicoOrdemUseCase iniciarServicoOrdemUseCase;

	private final FinalizarServicoOrdemUseCase finalizarServicoOrdemUseCase;

	private final IniciarDiagnosticoUseCase iniciarDiagnosticoUseCase;

	private final EncerrarDiagnosticoUseCase encerrarDiagnosticoUseCase;

	private final AdicionarItemOrdemServicoUseCase adicionarItemOrdemServicoUseCase;

	private final EncerrarComposicaoTecnicaUseCase encerrarComposicaoTecnicaUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param criarOrdemServicoUseCase caso de uso de criacao da OS.
	 * @param buscarOrdemServicoPorIdUseCase caso de uso de busca por id.
	 * @param listarOrdensServicoUseCase caso de uso de listagem administrativa.
	 * @param listarMinhasOrdensServicoUseCase caso de uso de listagem para clientes.
	 * @param consultarStatusOrdemServicoUseCase caso de uso de consulta de status pelo
	 * cliente.
	 * @param iniciarExecucaoUseCase caso de uso para iniciar execucao tecnica.
	 * @param finalizarExecucaoUseCase caso de uso para finalizar execucao tecnica.
	 * @param entregarVeiculoUseCase caso de uso para registrar entrega do veiculo.
	 * @param consultarHistoricoOrdemServicoUseCase caso de uso para consultar historico
	 * de transicoes.
	 */
	public OrdemServicoController(CriarOrdemServicoUseCase criarOrdemServicoUseCase,
			CriarOrdemServicoComItensUseCase criarOrdemServicoComItensUseCase,
			BuscarOrdemServicoPorIdUseCase buscarOrdemServicoPorIdUseCase,
			ListarOrdensServicoUseCase listarOrdensServicoUseCase,
			ListarMinhasOrdensServicoUseCase listarMinhasOrdensServicoUseCase,
			ConsultarStatusOrdemServicoUseCase consultarStatusOrdemServicoUseCase,
			IniciarExecucaoUseCase iniciarExecucaoUseCase, FinalizarExecucaoUseCase finalizarExecucaoUseCase,
			EntregarVeiculoUseCase entregarVeiculoUseCase,
			ConsultarHistoricoOrdemServicoUseCase consultarHistoricoOrdemServicoUseCase,
			IniciarServicoOrdemUseCase iniciarServicoOrdemUseCase,
			FinalizarServicoOrdemUseCase finalizarServicoOrdemUseCase,
			IniciarDiagnosticoUseCase iniciarDiagnosticoUseCase, EncerrarDiagnosticoUseCase encerrarDiagnosticoUseCase,
			AdicionarItemOrdemServicoUseCase adicionarItemOrdemServicoUseCase,
			EncerrarComposicaoTecnicaUseCase encerrarComposicaoTecnicaUseCase) {
		this.criarOrdemServicoUseCase = criarOrdemServicoUseCase;
		this.criarOrdemServicoComItensUseCase = criarOrdemServicoComItensUseCase;
		this.buscarOrdemServicoPorIdUseCase = buscarOrdemServicoPorIdUseCase;
		this.listarOrdensServicoUseCase = listarOrdensServicoUseCase;
		this.listarMinhasOrdensServicoUseCase = listarMinhasOrdensServicoUseCase;
		this.consultarStatusOrdemServicoUseCase = consultarStatusOrdemServicoUseCase;
		this.iniciarExecucaoUseCase = iniciarExecucaoUseCase;
		this.finalizarExecucaoUseCase = finalizarExecucaoUseCase;
		this.entregarVeiculoUseCase = entregarVeiculoUseCase;
		this.consultarHistoricoOrdemServicoUseCase = consultarHistoricoOrdemServicoUseCase;
		this.iniciarServicoOrdemUseCase = iniciarServicoOrdemUseCase;
		this.finalizarServicoOrdemUseCase = finalizarServicoOrdemUseCase;
		this.iniciarDiagnosticoUseCase = iniciarDiagnosticoUseCase;
		this.encerrarDiagnosticoUseCase = encerrarDiagnosticoUseCase;
		this.adicionarItemOrdemServicoUseCase = adicionarItemOrdemServicoUseCase;
		this.encerrarComposicaoTecnicaUseCase = encerrarComposicaoTecnicaUseCase;
	}

	/**
	 * Abre uma nova Ordem de Servico na recepcao do veiculo.
	 * @param request dados da recepcao.
	 * @return OS criada em status recebido, ainda sem itens ou orcamento.
	 */
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	@Operation(summary = "Abrir Ordem de Servico",
			description = "Registra a recepcao do veiculo e cria uma OS sem itens em status RECEBIDO.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "OS criada com sucesso",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados de entrada invalidos"),
			@ApiResponse(responseCode = "404", description = "Cliente nao encontrado"),
			@ApiResponse(responseCode = "422",
					description = "Regra de negocio violada (veiculo de outro cliente, estoque insuficiente, etc.)") })
	public ResponseEntity<OrdemServicoResponse> criar(@RequestBody @Valid CriarOrdemServicoRequest request) {
		ResultadoCriacaoOrdemServico resultado = criarOrdemServicoUseCase.executar(toDadosCriacao(request));
		return ResponseEntity.status(HttpStatus.CREATED).body(OrdemServicoResponse.from(resultado));
	}

	private DadosCriacaoOrdemServico toDadosCriacao(CriarOrdemServicoRequest request) {
		CriarOrdemServicoRequest.DadosVeiculoRequest veiculo = request.getVeiculo();
		return new DadosCriacaoOrdemServico(request.getClienteDocumento(), request.getVeiculoPlaca(),
				veiculo != null ? veiculo.getMarca() : null, veiculo != null ? veiculo.getModelo() : null,
				veiculo != null ? veiculo.getAno() : null, request.getObservacoes());
	}

	/**
	 * Abre uma Ordem de Servico com servicos e pecas iniciais.
	 * @param request dados completos da recepcao.
	 * @return OS recebida com itens persistidos e sem orcamento.
	 */
	@PostMapping("/com-itens")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	@Operation(summary = "Abrir Ordem de Servico com itens",
			description = "Cria uma OS em RECEBIDO com servicos, pecas ou ambos. "
					+ "O orcamento e gerado somente ao encerrar a composicao tecnica.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "OS criada com itens iniciais",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados de entrada invalidos"),
			@ApiResponse(responseCode = "404", description = "Cliente, servico ou peca nao encontrado"),
			@ApiResponse(responseCode = "422", description = "Regra de negocio ou estoque insuficiente") })
	public ResponseEntity<OrdemServicoResponse> criarComItens(
			@RequestBody @Valid CriarOrdemServicoComItensRequest request) {
		ResultadoCriacaoOrdemServico resultado = criarOrdemServicoComItensUseCase.executar(toDadosCriacao(request));
		return ResponseEntity.status(HttpStatus.CREATED).body(OrdemServicoResponse.from(resultado));
	}

	private DadosCriacaoOrdemServicoComItens toDadosCriacao(CriarOrdemServicoComItensRequest request) {
		CriarOrdemServicoRequest.DadosVeiculoRequest veiculo = request.getVeiculo();
		List<ItemServicoSolicitado> servicos = request.getServicos() == null ? List.of()
				: request.getServicos()
					.stream()
					.map(item -> new ItemServicoSolicitado(item.getServicoId(), item.getQuantidade()))
					.toList();
		List<ItemPecaSolicitada> pecas = request.getPecas() == null ? List.of()
				: request.getPecas()
					.stream()
					.map(item -> new ItemPecaSolicitada(item.getPecaId(), item.getQuantidade()))
					.toList();
		return new DadosCriacaoOrdemServicoComItens(request.getClienteDocumento(), request.getVeiculoPlaca(),
				veiculo != null ? veiculo.getMarca() : null, veiculo != null ? veiculo.getModelo() : null,
				veiculo != null ? veiculo.getAno() : null, servicos, pecas, request.getObservacoes());
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
	 * Lista a fila de trabalho: ordens ordenadas por prioridade de status (EM_EXECUCAO,
	 * AGUARDANDO_APROVACAO, EM_DIAGNOSTICO, RECEBIDO) e, dentro do mesmo status, mais
	 * antigas primeiro. Ordens FINALIZADA, ENTREGUE e CANCELADA sao excluidas
	 * logicamente.
	 * @param pagina pagina solicitada (zero-based).
	 * @param tamanho tamanho da pagina.
	 * @return resposta paginada com a fila de trabalho priorizada.
	 */
	@GetMapping("/fila-trabalho")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	@Operation(summary = "Listar fila de trabalho",
			description = "Lista as ordens ativas ordenadas por prioridade de status e antiguidade, "
					+ "excluindo as ordens finalizadas, entregues e canceladas.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Pagina da fila de trabalho",
			content = @Content(schema = @Schema(implementation = PaginaOrdensServicoResponse.class))) })
	public ResponseEntity<PaginaOrdensServicoResponse> listarFilaTrabalho(@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanho) {
		PaginaResultado<OrdemServico> resultado = listarOrdensServicoUseCase.executarFilaTrabalho(pagina, tamanho);
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
	 * Consulta o status atual de uma ordem de servico pelo numero. Endpoint publico.
	 * @param numero numero da OS (formato OS-{ANO}-{NNNNN}).
	 * @return status da OS com itens da composicao tecnica.
	 */
	@GetMapping("/{numero}/status")
	@Operation(summary = "Consultar status da Ordem de Servico",
			description = "Endpoint publico que permite consultar o status e os itens de uma OS pelo numero sequencial.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Status atual da OS com itens",
					content = @Content(schema = @Schema(implementation = StatusOrdemServicoResponse.class))),
			@ApiResponse(responseCode = "404", description = "OS nao encontrada") })
	public ResponseEntity<StatusOrdemServicoResponse> consultarStatus(@PathVariable String numero) {
		OrdemServico ordem = consultarStatusOrdemServicoUseCase.executar(numero);
		return ResponseEntity.ok(StatusOrdemServicoResponse.from(ordem));
	}

	@PatchMapping("/{id}/iniciar-diagnostico")
	@PreAuthorize("hasRole('MECANICO')")
	@Operation(summary = "Iniciar diagnostico da OS",
			description = "Mecanico inicia a inspecao tecnica do veiculo apos a recepcao. Restrito ao perfil MECANICO.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Diagnostico iniciado",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada"),
			@ApiResponse(responseCode = "422", description = "OS nao esta em RECEBIDO") })
	public ResponseEntity<OrdemServicoResponse> iniciarDiagnostico(@PathVariable UUID id) {
		OrdemServico ordemServico = iniciarDiagnosticoUseCase.executar(id);
		return ResponseEntity.ok(OrdemServicoResponse.from(ordemServico));
	}

	@PatchMapping("/{id}/encerrar-diagnostico")
	@PreAuthorize("hasRole('MECANICO')")
	@Operation(summary = "Encerrar diagnostico da OS",
			description = "Mecanico encerra a inspecao tecnica e avanca a OS para composicao tecnica. Restrito ao perfil MECANICO.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Diagnostico encerrado",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada"),
			@ApiResponse(responseCode = "422", description = "OS nao esta em EM_DIAGNOSTICO") })
	public ResponseEntity<OrdemServicoResponse> encerrarDiagnostico(@PathVariable UUID id) {
		OrdemServico ordemServico = encerrarDiagnosticoUseCase.executar(id);
		return ResponseEntity.ok(OrdemServicoResponse.from(ordemServico));
	}

	@PostMapping("/{id}/itens")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	@Operation(summary = "Adicionar item a composicao tecnica",
			description = "Adiciona um servico ou peca a composicao tecnica de uma OS em EM_COMPOSICAO.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Item adicionado",
					content = @Content(schema = @Schema(implementation = OrdemServicoDetalheResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico, servico ou peca nao encontrados"),
			@ApiResponse(responseCode = "422", description = "OS nao esta em EM_COMPOSICAO ou payload invalido") })
	public ResponseEntity<OrdemServicoDetalheResponse> adicionarItem(@PathVariable UUID id,
			@RequestBody @Valid AdicionarItemOrdemServicoRequest request) {
		OrdemServico ordemServico;
		if (request.getTipo() == AdicionarItemOrdemServicoRequest.TipoItem.SERVICO) {
			if (request.getServicoId() == null) {
				throw new RegraDeNegocioException(
						"O identificador do servico e obrigatorio para itens do tipo SERVICO.");
			}
			ordemServico = adicionarItemOrdemServicoUseCase.executarServico(id, request.getServicoId(),
					request.getQuantidade());
		}
		else {
			if (request.getPecaId() == null) {
				throw new RegraDeNegocioException("O identificador da peca e obrigatorio para itens do tipo PECA.");
			}
			ordemServico = adicionarItemOrdemServicoUseCase.executarPeca(id, request.getPecaId(),
					request.getQuantidade());
		}
		return ResponseEntity.ok(OrdemServicoDetalheResponse.from(ordemServico));
	}

	@PatchMapping("/{id}/encerrar-composicao")
	@PreAuthorize("hasRole('MECANICO')")
	@Operation(summary = "Encerrar composicao tecnica da OS",
			description = "Encerra a composicao tecnica e gera um orcamento aguardando aprovacao. Restrito ao perfil MECANICO.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Composicao encerrada e orcamento gerado",
					content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada"),
			@ApiResponse(responseCode = "422", description = "OS nao esta em EM_COMPOSICAO ou sem itens") })
	public ResponseEntity<OrdemServicoResponse> encerrarComposicao(@PathVariable UUID id) {
		encerrarComposicaoTecnicaUseCase.executar(id);
		OrdemServico ordemServico = buscarOrdemServicoPorIdUseCase.executar(id);
		return ResponseEntity.ok(OrdemServicoResponse.from(ordemServico));
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

	@PatchMapping("/{id}/itens/{idItem}/iniciar-servico")
	@PreAuthorize("hasRole('MECANICO')")
	@Operation(summary = "Iniciar execucao de um servico da OS",
			description = "Marca o inicio da execucao de um item de servico individual dentro da ordem. "
					+ "Restrito ao perfil MECANICO. Apenas um servico pode estar em execucao por OS por vez.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Servico iniciado com sucesso",
					content = @Content(schema = @Schema(implementation = OrdemServicoDetalheResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada"),
			@ApiResponse(responseCode = "422",
					description = "OS nao esta em execucao, item nao e servico, servico ja iniciado ou outro servico em execucao") })
	public ResponseEntity<OrdemServicoDetalheResponse> iniciarServico(@PathVariable UUID id,
			@PathVariable UUID idItem) {
		OrdemServico ordemServico = iniciarServicoOrdemUseCase.executar(id, idItem);
		return ResponseEntity.ok(OrdemServicoDetalheResponse.from(ordemServico));
	}

	@PatchMapping("/{id}/itens/{idItem}/finalizar-servico")
	@PreAuthorize("hasRole('MECANICO')")
	@Operation(summary = "Finalizar execucao de um servico da OS",
			description = "Marca a finalizacao da execucao de um item de servico individual dentro da ordem. "
					+ "Restrito ao perfil MECANICO.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Servico finalizado com sucesso",
					content = @Content(schema = @Schema(implementation = OrdemServicoDetalheResponse.class))),
			@ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada"),
			@ApiResponse(responseCode = "422", description = "Servico nao esta em execucao ou item nao e servico") })
	public ResponseEntity<OrdemServicoDetalheResponse> finalizarServico(@PathVariable UUID id,
			@PathVariable UUID idItem) {
		OrdemServico ordemServico = finalizarServicoOrdemUseCase.executar(id, idItem);
		return ResponseEntity.ok(OrdemServicoDetalheResponse.from(ordemServico));
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
