package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.OrdemServicoResponse;
import com.postech.workshop_service.api.dtos.OrdemServicoResponse.ClienteResumoResponse;
import com.postech.workshop_service.api.dtos.OrdemServicoResponse.OrcamentoResumoResponse;
import com.postech.workshop_service.api.dtos.OrdemServicoResponse.VeiculoResumoResponse;
import com.postech.workshop_service.application.usecases.CriarOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.ItemPecaSolicitada;
import com.postech.workshop_service.application.usecases.ItemServicoSolicitado;
import com.postech.workshop_service.application.usecases.ResultadoCriacaoOrdemServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller responsavel pelas operacoes de Ordens de Servico.
 */
@RestController
@RequestMapping("/api/v1/ordens-servico")
@Tag(name = "Ordens de Servico", description = "Gerenciamento do ciclo de vida das ordens de servico da oficina")
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
		List<ItemServicoSolicitado> servicos = request.getServicos()
			.stream()
			.map(s -> new ItemServicoSolicitado(s.getServicoId(), s.getQuantidade()))
			.toList();

		List<ItemPecaSolicitada> pecas = request.getPecas() != null ? request.getPecas()
			.stream()
			.map(p -> new ItemPecaSolicitada(p.getPecaId(), p.getQuantidade()))
			.toList() : List.of();

		CriarOrdemServicoRequest.DadosVeiculoRequest dadosVeiculo = request.getVeiculo();

		ResultadoCriacaoOrdemServico resultado = criarOrdemServicoUseCase.executar(request.getClienteDocumento(),
				request.getVeiculoPlaca(), dadosVeiculo != null ? dadosVeiculo.getMarca() : null,
				dadosVeiculo != null ? dadosVeiculo.getModelo() : null,
				dadosVeiculo != null ? dadosVeiculo.getAno() : null, servicos, pecas, request.getObservacoes());

		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(resultado));
	}

	private OrdemServicoResponse toResponse(ResultadoCriacaoOrdemServico resultado) {
		return OrdemServicoResponse.builder()
			.id(resultado.ordemServico().getId())
			.numero(resultado.ordemServico().getNumero())
			.status(resultado.ordemServico().getStatus().name())
			.cliente(ClienteResumoResponse.builder()
				.id(resultado.cliente().getId())
				.nome(resultado.cliente().getNome())
				.documentoMascarado(resultado.cliente().getDocumento().mascarado())
				.build())
			.veiculo(VeiculoResumoResponse.builder()
				.id(resultado.veiculo().getId())
				.placa(resultado.veiculo().getPlaca().getValor())
				.marca(resultado.veiculo().getMarca())
				.modelo(resultado.veiculo().getModelo())
				.ano(resultado.veiculo().getAno())
				.build())
			.orcamento(OrcamentoResumoResponse.builder()
				.id(resultado.orcamento().getId())
				.valorTotal(resultado.orcamento().getValor())
				.status(resultado.orcamento().getStatus().name())
				.dataCriacao(resultado.orcamento().getDataCriacao())
				.build())
			.observacoes(resultado.ordemServico().getObservacoes())
			.dataCriacao(resultado.ordemServico().getDataCriacao())
			.dataUltimaAtualizacao(resultado.ordemServico().getDataUltimaAtualizacao())
			.build();
	}

}
