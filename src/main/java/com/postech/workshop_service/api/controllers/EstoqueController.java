package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.EstoqueResponse;
import com.postech.workshop_service.api.dtos.MovimentacaoRequest;
import com.postech.workshop_service.api.dtos.MovimentacaoResponse;
import com.postech.workshop_service.application.usecases.BuscarEstoquePorIdUseCase;
import com.postech.workshop_service.application.usecases.ListarEstoquesPorPecaUseCase;
import com.postech.workshop_service.application.usecases.RegistrarMovimentacaoUseCase;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller responsavel por estoques e movimentacoes.
 */
@RestController
@RequestMapping("/api/v1/estoques")
@Tag(name = "Estoques", description = "Gerenciamento de estoques e movimentacoes")
@SecurityRequirement(name = "bearerAuth")
public class EstoqueController {

	private final BuscarEstoquePorIdUseCase buscarEstoquePorIdUseCase;

	private final ListarEstoquesPorPecaUseCase listarEstoquesPorPecaUseCase;

	private final RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param buscarEstoquePorIdUseCase caso de uso de busca por ID.
	 * @param listarEstoquesPorPecaUseCase caso de uso de listagem por peca.
	 * @param registrarMovimentacaoUseCase caso de uso de movimentacao.
	 */
	public EstoqueController(BuscarEstoquePorIdUseCase buscarEstoquePorIdUseCase,
			ListarEstoquesPorPecaUseCase listarEstoquesPorPecaUseCase,
			RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase) {
		this.buscarEstoquePorIdUseCase = buscarEstoquePorIdUseCase;
		this.listarEstoquesPorPecaUseCase = listarEstoquesPorPecaUseCase;
		this.registrarMovimentacaoUseCase = registrarMovimentacaoUseCase;
	}

	/**
	 * Endpoint para registrar uma movimentacao de estoque.
	 * @param request dados da movimentacao.
	 * @return resposta com os dados da movimentacao registrada.
	 */
	@PostMapping("/movimentacoes")
	@Operation(summary = "Registrar uma movimentacao de estoque")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MECANICO')")
	public ResponseEntity<MovimentacaoResponse> registrarMovimentacao(@RequestBody @Valid MovimentacaoRequest request) {
		MovimentacaoEstoque movimentacao = registrarMovimentacaoUseCase.executar(request.getEstoqueId(),
				request.getTipo(), request.getQuantidade(), request.getMotivo());
		return ResponseEntity.status(HttpStatus.CREATED).body(toMovimentacaoResponse(movimentacao));
	}

	/**
	 * Endpoint para buscar um estoque por ID.
	 * @param id identificador do estoque.
	 * @return dados do estoque.
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Buscar estoque por ID")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	public ResponseEntity<EstoqueResponse> buscarPorId(@PathVariable UUID id,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		return buscarEstoquePorIdUseCase.executar(id, incluirInativos)
			.map(estoque -> ResponseEntity.ok(toEstoqueResponse(estoque)))
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint para listar estoques de uma peca.
	 * @param pecaInsumoId identificador da peca.
	 * @return lista de estoques da peca.
	 */
	@GetMapping("/peca/{pecaInsumoId}")
	@Operation(summary = "Listar estoques de uma peca")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MECANICO')")
	public ResponseEntity<List<EstoqueResponse>> listarPorPeca(@PathVariable UUID pecaInsumoId,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		List<EstoqueResponse> lista = listarEstoquesPorPecaUseCase.executar(pecaInsumoId, incluirInativos)
			.stream()
			.map(this::toEstoqueResponse)
			.collect(Collectors.toList());
		return ResponseEntity.ok(lista);
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

	private MovimentacaoResponse toMovimentacaoResponse(MovimentacaoEstoque movimentacao) {
		return MovimentacaoResponse.builder()
			.id(movimentacao.getId())
			.estoqueId(movimentacao.getEstoqueId())
			.tipo(movimentacao.getTipo().name())
			.quantidade(movimentacao.getQuantidade())
			.quantidadeAnterior(movimentacao.getQuantidadeAnterior())
			.quantidadePosterior(movimentacao.getQuantidadePosterior())
			.motivo(movimentacao.getMotivo())
			.dataMovimentacao(movimentacao.getDataMovimentacao())
			.build();
	}

}
