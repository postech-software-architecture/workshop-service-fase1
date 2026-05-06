package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.AtualizarPecaRequest;
import com.postech.workshop_service.api.dtos.CadastroPecaRequest;
import com.postech.workshop_service.api.dtos.CriarEstoqueRequest;
import com.postech.workshop_service.api.dtos.EstoqueResponse;
import com.postech.workshop_service.api.dtos.PaginaPecasResponse;
import com.postech.workshop_service.api.dtos.PecaResponse;
import com.postech.workshop_service.application.usecases.AtualizarPecaUseCase;
import com.postech.workshop_service.application.usecases.BuscarPecaPorIdUseCase;
import com.postech.workshop_service.application.usecases.BuscarPecaPorSkuUseCase;
import com.postech.workshop_service.application.usecases.CriarEstoqueUseCase;
import com.postech.workshop_service.application.usecases.CriarPecaUseCase;
import com.postech.workshop_service.application.usecases.ListarPecasUseCase;
import com.postech.workshop_service.application.usecases.RemoverPecaUseCase;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller responsavel por pecas, insumos e criacao de estoques.
 */
@RestController
@RequestMapping("/api/v1/pecas")
@Tag(name = "Pecas e Insumos", description = "Gerenciamento de pecas e insumos da oficina")
@SecurityRequirement(name = "bearerAuth")
public class PecaInsumoController {

	private final CriarPecaUseCase criarPecaUseCase;

	private final AtualizarPecaUseCase atualizarPecaUseCase;

	private final BuscarPecaPorIdUseCase buscarPecaPorIdUseCase;

	private final BuscarPecaPorSkuUseCase buscarPecaPorSkuUseCase;

	private final ListarPecasUseCase listarPecasUseCase;

	private final RemoverPecaUseCase removerPecaUseCase;

	private final CriarEstoqueUseCase criarEstoqueUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param criarPecaUseCase caso de uso de criacao.
	 * @param atualizarPecaUseCase caso de uso de atualizacao.
	 * @param buscarPecaPorIdUseCase caso de uso de busca por ID.
	 * @param buscarPecaPorSkuUseCase caso de uso de busca por SKU.
	 * @param listarPecasUseCase caso de uso de listagem.
	 * @param removerPecaUseCase caso de uso de remocao.
	 * @param criarEstoqueUseCase caso de uso de criacao de estoque.
	 */
	public PecaInsumoController(CriarPecaUseCase criarPecaUseCase, AtualizarPecaUseCase atualizarPecaUseCase,
			BuscarPecaPorIdUseCase buscarPecaPorIdUseCase, BuscarPecaPorSkuUseCase buscarPecaPorSkuUseCase,
			ListarPecasUseCase listarPecasUseCase, RemoverPecaUseCase removerPecaUseCase,
			CriarEstoqueUseCase criarEstoqueUseCase) {
		this.criarPecaUseCase = criarPecaUseCase;
		this.atualizarPecaUseCase = atualizarPecaUseCase;
		this.buscarPecaPorIdUseCase = buscarPecaPorIdUseCase;
		this.buscarPecaPorSkuUseCase = buscarPecaPorSkuUseCase;
		this.listarPecasUseCase = listarPecasUseCase;
		this.removerPecaUseCase = removerPecaUseCase;
		this.criarEstoqueUseCase = criarEstoqueUseCase;
	}

	/**
	 * Endpoint para cadastrar uma nova peca.
	 * @param request dados da peca a ser cadastrada.
	 * @return resposta com os dados da peca criada e status 201.
	 */
	@PostMapping
	@Operation(summary = "Criar uma nova peca ou insumo")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<PecaResponse> criar(@RequestBody @Valid CadastroPecaRequest request) {
		PecaInsumo peca = criarPecaUseCase.executar(request.getSku(), request.getNome(), request.getValorUnitario(),
				request.getEstoqueMinimo(), request.getUnidadeMedida(), request.getTipoItem(), request.getFornecedor(),
				request.getCodigoBarras(), request.getMarca(), request.getCategoria(), request.getAplicacao(),
				request.getObservacoes());
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(peca, BigDecimal.ZERO));
	}

	/**
	 * Endpoint para atualizar os dados de uma peca existente.
	 * @param id identificador unico da peca.
	 * @param request novos dados da peca.
	 * @return resposta com os dados atualizados.
	 */
	@PutMapping("/{id}")
	@Operation(summary = "Atualizar dados de uma peca")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<PecaResponse> atualizar(@PathVariable UUID id,
			@RequestBody @Valid AtualizarPecaRequest request) {
		PecaInsumo peca = atualizarPecaUseCase.executar(id, request.getNome(), request.getValorUnitario(),
				request.getEstoqueMinimo(), request.getUnidadeMedida(), request.getTipoItem(), request.getFornecedor(),
				request.getCodigoBarras(), request.getMarca(), request.getCategoria(), request.getAplicacao(),
				request.getObservacoes());
		BigDecimal quantidadeTotal = listarPecasUseCase.calcularQuantidadeTotal(peca.getId());
		return ResponseEntity.ok(toResponse(peca, quantidadeTotal));
	}

	/**
	 * Endpoint para buscar uma peca pelo seu identificador unico.
	 * @param id identificador unico.
	 * @return dados da peca ou 404 caso nao encontrada.
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Buscar peca por ID")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	public ResponseEntity<PecaResponse> buscarPorId(@PathVariable UUID id,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		return buscarPecaPorIdUseCase.executar(id, incluirInativos).map(peca -> {
			BigDecimal quantidadeTotal = listarPecasUseCase.calcularQuantidadeTotal(peca.getId());
			return ResponseEntity.ok(toResponse(peca, quantidadeTotal));
		}).orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint para buscar uma peca pelo seu SKU.
	 * @param sku codigo SKU da peca.
	 * @return dados da peca ou 404 caso nao encontrada.
	 */
	@GetMapping("/sku/{sku}")
	@Operation(summary = "Buscar peca por SKU")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	public ResponseEntity<PecaResponse> buscarPorSku(@PathVariable String sku,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		return buscarPecaPorSkuUseCase.executar(sku, incluirInativos).map(peca -> {
			BigDecimal quantidadeTotal = listarPecasUseCase.calcularQuantidadeTotal(peca.getId());
			return ResponseEntity.ok(toResponse(peca, quantidadeTotal));
		}).orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint para listar pecas de forma paginada.
	 * @param pagina numero da pagina.
	 * @param tamanho registros por pagina.
	 * @param nome filtro por nome (opcional).
	 * @param categoria filtro por categoria (opcional).
	 * @return lista paginada de pecas.
	 */
	@GetMapping
	@Operation(summary = "Listar pecas com paginacao e filtros")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	public ResponseEntity<PaginaPecasResponse> listar(@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanho, @RequestParam(required = false) String nome,
			@RequestParam(required = false) String categoria,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		PaginaResultado<PecaInsumo> resultado = listarPecasUseCase.executar(pagina, tamanho, nome, categoria,
				incluirInativos);
		List<PecaResponse> lista = resultado.itens().stream().map(peca -> {
			BigDecimal quantidadeTotal = listarPecasUseCase.calcularQuantidadeTotal(peca.getId());
			return toResponse(peca, quantidadeTotal);
		}).collect(Collectors.toList());
		return ResponseEntity.ok(PaginaPecasResponse.builder()
			.conteudo(lista)
			.pagina(resultado.pagina())
			.tamanho(resultado.tamanho())
			.totalElementos(resultado.totalElementos())
			.totalPaginas(resultado.totalPaginas())
			.build());
	}

	/**
	 * Endpoint para remover uma peca do sistema.
	 * @param id identificador unico.
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Remover uma peca (soft delete)")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public void remover(@PathVariable UUID id) {
		removerPecaUseCase.executar(id);
	}

	/**
	 * Endpoint para criar um novo estoque para uma peca.
	 * @param request dados do estoque.
	 * @return resposta com os dados do estoque criado.
	 */
	@PostMapping("/estoques")
	@Operation(summary = "Criar um novo estoque para uma peca")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<EstoqueResponse> criarEstoque(@RequestBody @Valid CriarEstoqueRequest request) {
		Estoque estoque = criarEstoqueUseCase.executar(request.getPecaInsumoId(), request.getLocalizacao(),
				request.getQuantidade());
		return ResponseEntity.status(HttpStatus.CREATED).body(toEstoqueResponse(estoque));
	}

	private PecaResponse toResponse(PecaInsumo peca, BigDecimal quantidadeTotal) {
		return PecaResponse.builder()
			.id(peca.getId())
			.sku(peca.getSku())
			.nome(peca.getNome())
			.valorUnitario(peca.getValorUnitario())
			.estoqueMinimo(peca.getEstoqueMinimo())
			.unidadeMedida(peca.getUnidadeMedida().name())
			.tipoItem(peca.getTipoItem().name())
			.fornecedor(peca.getFornecedor())
			.codigoBarras(peca.getCodigoBarras())
			.marca(peca.getMarca())
			.categoria(peca.getCategoria())
			.aplicacao(peca.getAplicacao())
			.observacoes(peca.getObservacoes())
			.ativo(peca.isAtivo())
			.quantidadeTotal(quantidadeTotal)
			.versao(peca.getVersao())
			.dataCriacao(peca.getDataCriacao())
			.dataUltimaAtualizacao(peca.getDataUltimaAtualizacao())
			.dataRemocao(peca.getDataRemocao())
			.build();
	}

	private EstoqueResponse toEstoqueResponse(Estoque estoque) {
		return EstoqueResponse.builder()
			.id(estoque.getId())
			.pecaInsumoId(estoque.getPecaInsumoId())
			.localizacao(estoque.getLocalizacao())
			.quantidade(estoque.getQuantidade())
			.ativo(estoque.isAtivo())
			.versao(estoque.getVersao())
			.dataCriacao(estoque.getDataCriacao())
			.dataUltimaAtualizacao(estoque.getDataUltimaAtualizacao())
			.build();
	}

}
