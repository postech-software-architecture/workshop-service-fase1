package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.AtualizarServicoRequest;
import com.postech.workshop_service.api.dtos.CadastroServicoRequest;
import com.postech.workshop_service.api.dtos.PaginaServicosResponse;
import com.postech.workshop_service.api.dtos.ServicoResponse;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.usecases.AtualizarServicoUseCase;
import com.postech.workshop_service.application.usecases.BuscarServicoPorIdUseCase;
import com.postech.workshop_service.application.usecases.CriarServicoUseCase;
import com.postech.workshop_service.application.usecases.ListarServicosPorCategoriaUseCase;
import com.postech.workshop_service.application.usecases.ListarServicosUseCase;
import com.postech.workshop_service.application.usecases.ReativarServicoUseCase;
import com.postech.workshop_service.application.usecases.RemoverServicoUseCase;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.util.List;
import java.util.UUID;

/**
 * Controller responsavel pelas operacoes do catalogo de servicos da oficina.
 */
@RestController
@RequestMapping("/api/v1/servicos")
@Tag(name = "Servicos", description = "Gerenciamento do catálogo de serviços da oficina")
public class ServicoController {

	private final CriarServicoUseCase criarServicoUseCase;

	private final AtualizarServicoUseCase atualizarServicoUseCase;

	private final BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;

	private final ListarServicosUseCase listarServicosUseCase;

	private final ListarServicosPorCategoriaUseCase listarServicosPorCategoriaUseCase;

	private final RemoverServicoUseCase removerServicoUseCase;

	private final ReativarServicoUseCase reativarServicoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param criarServicoUseCase caso de uso de criacao.
	 * @param atualizarServicoUseCase caso de uso de atualizacao.
	 * @param buscarServicoPorIdUseCase caso de uso de busca por identificador.
	 * @param listarServicosUseCase caso de uso de listagem paginada.
	 * @param listarServicosPorCategoriaUseCase caso de uso de listagem por categoria.
	 * @param removerServicoUseCase caso de uso de remocao logica.
	 * @param reativarServicoUseCase caso de uso de reativacao logica.
	 */
	public ServicoController(CriarServicoUseCase criarServicoUseCase, AtualizarServicoUseCase atualizarServicoUseCase,
			BuscarServicoPorIdUseCase buscarServicoPorIdUseCase, ListarServicosUseCase listarServicosUseCase,
			ListarServicosPorCategoriaUseCase listarServicosPorCategoriaUseCase,
			RemoverServicoUseCase removerServicoUseCase, ReativarServicoUseCase reativarServicoUseCase) {
		this.criarServicoUseCase = criarServicoUseCase;
		this.atualizarServicoUseCase = atualizarServicoUseCase;
		this.buscarServicoPorIdUseCase = buscarServicoPorIdUseCase;
		this.listarServicosUseCase = listarServicosUseCase;
		this.listarServicosPorCategoriaUseCase = listarServicosPorCategoriaUseCase;
		this.removerServicoUseCase = removerServicoUseCase;
		this.reativarServicoUseCase = reativarServicoUseCase;
	}

	/**
	 * Cadastra um novo servico no catalogo.
	 * @param request payload do novo servico.
	 * @return servico persistido.
	 */
	@PostMapping
	@Operation(summary = "Cadastrar serviço no catálogo")
	public ResponseEntity<ServicoResponse> criar(@RequestBody @Valid CadastroServicoRequest request) {
		Servico servico = criarServicoUseCase.executar(request.getNome(), request.getDescricao(), request.getValor(),
				request.getCategoria(), request.getNivelComplexidade(), request.getGarantiaDias(),
				request.getObservacoesTecnicas());
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(servico));
	}

	/**
	 * Lista servicos com filtros opcionais e paginacao.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho solicitado.
	 * @param nome nome opcional para filtro parcial.
	 * @param categoria categoria opcional para filtro exato.
	 * @param incluirInativos indica se servicos inativos devem ser considerados.
	 * @return pagina de servicos.
	 */
	@GetMapping
	@Operation(summary = "Listar serviços com paginação e filtros")
	public ResponseEntity<PaginaServicosResponse> listar(
			@RequestParam(defaultValue = "0") @Parameter(description = "Página solicitada (base zero)") int pagina,
			@RequestParam(defaultValue = "20") @Parameter(
					description = "Quantidade de registros por página") int tamanho,
			@RequestParam(required = false) @Parameter(description = "Filtro parcial pelo nome do serviço") String nome,
			@RequestParam(required = false) @Parameter(
					description = "Filtro por categoria do serviço") CategoriaServico categoria,
			@RequestParam(defaultValue = "false") @Parameter(
					description = "Indica se serviços inativos devem ser considerados") boolean incluirInativos) {
		PaginaResultado<Servico> resultado = listarServicosUseCase.executar(pagina, tamanho, nome, categoria,
				incluirInativos);
		return ResponseEntity.ok(PaginaServicosResponse.builder()
			.conteudo(resultado.itens().stream().map(this::toResponse).toList())
			.pagina(resultado.pagina())
			.tamanho(resultado.tamanho())
			.totalElementos(resultado.totalElementos())
			.totalPaginas(resultado.totalPaginas())
			.build());
	}

	/**
	 * Busca um servico pelo identificador.
	 * @param id identificador do servico.
	 * @param incluirInativos indica se servicos inativos devem ser considerados.
	 * @return servico encontrado.
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Buscar serviço por identificador")
	public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable UUID id,
			@RequestParam(defaultValue = "false") @Parameter(
					description = "Indica se serviços inativos devem ser considerados") boolean incluirInativos) {
		Servico servico = buscarServicoPorIdUseCase.executar(id, incluirInativos)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID informado."));
		return ResponseEntity.ok(toResponse(servico));
	}

	/**
	 * Lista todos os servicos de uma categoria.
	 * @param categoria categoria dos servicos.
	 * @param incluirInativos indica se servicos inativos devem ser considerados.
	 * @return lista de servicos da categoria.
	 */
	@GetMapping("/categoria/{categoria}")
	@Operation(summary = "Listar serviços por categoria")
	public ResponseEntity<List<ServicoResponse>> listarPorCategoria(@PathVariable CategoriaServico categoria,
			@RequestParam(defaultValue = "false") @Parameter(
					description = "Indica se serviços inativos devem ser considerados") boolean incluirInativos) {
		List<ServicoResponse> respostas = listarServicosPorCategoriaUseCase.executar(categoria, incluirInativos)
			.stream()
			.map(this::toResponse)
			.toList();
		return ResponseEntity.ok(respostas);
	}

	/**
	 * Atualiza um servico existente no catalogo.
	 * @param id identificador do servico.
	 * @param request payload de atualizacao.
	 * @return servico atualizado.
	 */
	@PutMapping("/{id}")
	@Operation(summary = "Atualizar serviço do catálogo")
	public ResponseEntity<ServicoResponse> atualizar(@PathVariable UUID id,
			@RequestBody @Valid AtualizarServicoRequest request) {
		Servico servico = atualizarServicoUseCase.executar(id, request.getNome(), request.getDescricao(),
				request.getValor(), request.getCategoria(), request.getNivelComplexidade(), request.getGarantiaDias(),
				request.getObservacoesTecnicas());
		return ResponseEntity.ok(toResponse(servico));
	}

	/**
	 * Remove logicamente um servico do catalogo.
	 * @param id identificador do servico.
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Remover serviço logicamente")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable UUID id) {
		removerServicoUseCase.executar(id);
	}

	/**
	 * Reativa logicamente um servico previamente removido.
	 * @param id identificador do servico.
	 * @return servico reativado.
	 */
	@PostMapping("/{id}/reativar")
	@Operation(summary = "Reativar serviço previamente removido")
	public ResponseEntity<ServicoResponse> reativar(@PathVariable UUID id) {
		Servico servico = reativarServicoUseCase.executar(id);
		return ResponseEntity.ok(toResponse(servico));
	}

	private ServicoResponse toResponse(Servico servico) {
		return ServicoResponse.builder()
			.id(servico.getId())
			.nome(servico.getNome())
			.descricao(servico.getDescricao())
			.valor(servico.getValor())
			.categoria(servico.getCategoria())
			.nivelComplexidade(servico.getNivelComplexidade())
			.garantiaDias(servico.getGarantiaDias())
			.observacoesTecnicas(servico.getObservacoesTecnicas())
			.ativo(servico.isAtivo())
			.dataCriacao(servico.getDataCriacao())
			.dataUltimaAtualizacao(servico.getDataUltimaAtualizacao())
			.dataRemocao(servico.getDataRemocao())
			.build();
	}

}
