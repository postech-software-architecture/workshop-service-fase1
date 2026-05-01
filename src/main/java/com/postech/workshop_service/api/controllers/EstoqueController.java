package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.*;
import com.postech.workshop_service.application.usecases.*;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/estoques")
@Tag(name = "Estoques", description = "Gerenciamento de estoques e movimentacoes")
public class EstoqueController {

	private final EstoqueRepository estoqueRepository;

	private final RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param estoqueRepository repositorio de estoques.
	 * @param registrarMovimentacaoUseCase caso de uso de movimentacao.
	 */
	public EstoqueController(EstoqueRepository estoqueRepository,
			RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase) {
		this.estoqueRepository = estoqueRepository;
		this.registrarMovimentacaoUseCase = registrarMovimentacaoUseCase;
	}

	/**
	 * Endpoint para registrar uma movimentacao de estoque.
	 * @param request dados da movimentacao.
	 * @return resposta com os dados da movimentacao registrada.
	 */
	@PostMapping("/movimentacoes")
	@Operation(summary = "Registrar uma movimentacao de estoque")
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
	public ResponseEntity<EstoqueResponse> buscarPorId(@PathVariable UUID id,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		return estoqueRepository.buscarPorId(id, incluirInativos)
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
	public ResponseEntity<List<EstoqueResponse>> listarPorPeca(@PathVariable UUID pecaInsumoId,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		List<EstoqueResponse> lista = estoqueRepository.listarPorPeca(pecaInsumoId, incluirInativos)
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
