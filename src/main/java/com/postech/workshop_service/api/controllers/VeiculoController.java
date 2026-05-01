package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.AtualizarVeiculoRequest;
import com.postech.workshop_service.api.dtos.CadastroVeiculoRequest;
import com.postech.workshop_service.api.dtos.ClienteVinculadoResponse;
import com.postech.workshop_service.api.dtos.PaginaVeiculosResponse;
import com.postech.workshop_service.api.dtos.VeiculoResponse;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.usecases.AtualizarVeiculoUseCase;
import com.postech.workshop_service.application.usecases.BuscarClientePorIdUseCase;
import com.postech.workshop_service.application.usecases.BuscarVeiculoPorIdUseCase;
import com.postech.workshop_service.application.usecases.BuscarVeiculoPorPlacaUseCase;
import com.postech.workshop_service.application.usecases.CriarVeiculoUseCase;
import com.postech.workshop_service.application.usecases.DesvincularClienteVeiculoUseCase;
import com.postech.workshop_service.application.usecases.ListarVeiculosPorClienteUseCase;
import com.postech.workshop_service.application.usecases.ListarVeiculosUseCase;
import com.postech.workshop_service.application.usecases.RemoverVeiculoUseCase;
import com.postech.workshop_service.application.usecases.VincularClienteVeiculoUseCase;
import com.postech.workshop_service.domain.entities.Veiculo;
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

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Controller responsavel pelas operacoes de cadastro e consulta de veiculos.
 */
@RestController
@RequestMapping("/api/v1/veiculos")
@Tag(name = "Veiculos", description = "Gerenciamento de veículos da oficina")
public class VeiculoController {

	private final CriarVeiculoUseCase criarVeiculoUseCase;

	private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;

	private final BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase;

	private final BuscarVeiculoPorPlacaUseCase buscarVeiculoPorPlacaUseCase;

	private final ListarVeiculosUseCase listarVeiculosUseCase;

	private final ListarVeiculosPorClienteUseCase listarVeiculosPorClienteUseCase;

	private final RemoverVeiculoUseCase removerVeiculoUseCase;

	private final VincularClienteVeiculoUseCase vincularClienteVeiculoUseCase;

	private final DesvincularClienteVeiculoUseCase desvincularClienteVeiculoUseCase;

	private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param criarVeiculoUseCase caso de uso de criacao.
	 * @param atualizarVeiculoUseCase caso de uso de atualizacao.
	 * @param buscarVeiculoPorIdUseCase caso de uso de busca por identificador.
	 * @param buscarVeiculoPorPlacaUseCase caso de uso de busca por placa.
	 * @param listarVeiculosUseCase caso de uso de listagem paginada.
	 * @param listarVeiculosPorClienteUseCase caso de uso de listagem por cliente.
	 * @param removerVeiculoUseCase caso de uso de remocao logica.
	 * @param vincularClienteVeiculoUseCase caso de uso de vinculo de cliente.
	 * @param desvincularClienteVeiculoUseCase caso de uso de desvinculo de cliente.
	 * @param buscarClientePorIdUseCase caso de uso de busca de cliente para montar
	 * resposta.
	 */
	public VeiculoController(CriarVeiculoUseCase criarVeiculoUseCase, AtualizarVeiculoUseCase atualizarVeiculoUseCase,
			BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase,
			BuscarVeiculoPorPlacaUseCase buscarVeiculoPorPlacaUseCase, ListarVeiculosUseCase listarVeiculosUseCase,
			ListarVeiculosPorClienteUseCase listarVeiculosPorClienteUseCase,
			RemoverVeiculoUseCase removerVeiculoUseCase, VincularClienteVeiculoUseCase vincularClienteVeiculoUseCase,
			DesvincularClienteVeiculoUseCase desvincularClienteVeiculoUseCase,
			BuscarClientePorIdUseCase buscarClientePorIdUseCase) {
		this.criarVeiculoUseCase = criarVeiculoUseCase;
		this.atualizarVeiculoUseCase = atualizarVeiculoUseCase;
		this.buscarVeiculoPorIdUseCase = buscarVeiculoPorIdUseCase;
		this.buscarVeiculoPorPlacaUseCase = buscarVeiculoPorPlacaUseCase;
		this.listarVeiculosUseCase = listarVeiculosUseCase;
		this.listarVeiculosPorClienteUseCase = listarVeiculosPorClienteUseCase;
		this.removerVeiculoUseCase = removerVeiculoUseCase;
		this.vincularClienteVeiculoUseCase = vincularClienteVeiculoUseCase;
		this.desvincularClienteVeiculoUseCase = desvincularClienteVeiculoUseCase;
		this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
	}

	/**
	 * Cadastra um novo veiculo.
	 * @param request payload do novo veiculo.
	 * @return veiculo persistido.
	 */
	@PostMapping
	@Operation(summary = "Cadastrar veículo")
	public ResponseEntity<VeiculoResponse> criar(@RequestBody @Valid CadastroVeiculoRequest request) {
		Veiculo veiculo = criarVeiculoUseCase.executar(request.getPlaca(), request.getMarca(), request.getModelo(),
				request.getAno(), request.getCor(), request.getObservacoes(), request.getClientesIds());
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(veiculo));
	}

	/**
	 * Atualiza um veiculo existente.
	 * @param id identificador do veiculo.
	 * @param request payload de atualizacao.
	 * @return veiculo atualizado.
	 */
	@PutMapping("/{id}")
	@Operation(summary = "Atualizar veículo")
	public ResponseEntity<VeiculoResponse> atualizar(@PathVariable UUID id,
			@RequestBody @Valid AtualizarVeiculoRequest request) {
		Veiculo veiculo = atualizarVeiculoUseCase.executar(id, request.getPlaca(), request.getMarca(),
				request.getModelo(), request.getAno(), request.getCor(), request.getObservacoes());
		return ResponseEntity.ok(toResponse(veiculo));
	}

	/**
	 * Vincula um novo cliente a um veiculo existente.
	 * @param id identificador do veiculo.
	 * @param clienteId identificador do cliente.
	 * @return veiculo atualizado.
	 */
	@PostMapping("/{id}/clientes/{clienteId}")
	@Operation(summary = "Vincular cliente ao veículo")
	public ResponseEntity<VeiculoResponse> vincularCliente(@PathVariable UUID id, @PathVariable UUID clienteId) {
		Veiculo veiculo = vincularClienteVeiculoUseCase.executar(id, clienteId);
		return ResponseEntity.ok(toResponse(veiculo));
	}

	/**
	 * Desvincula um cliente de um veiculo existente.
	 * @param id identificador do veiculo.
	 * @param clienteId identificador do cliente.
	 * @return veiculo atualizado.
	 */
	@DeleteMapping("/{id}/clientes/{clienteId}")
	@Operation(summary = "Desvincular cliente do veículo")
	public ResponseEntity<VeiculoResponse> desvincularCliente(@PathVariable UUID id, @PathVariable UUID clienteId) {
		Veiculo veiculo = desvincularClienteVeiculoUseCase.executar(id, clienteId);
		return ResponseEntity.ok(toResponse(veiculo));
	}

	/**
	 * Busca um veiculo pelo identificador.
	 * @param id identificador do veiculo.
	 * @param incluirInativos indica se veiculos inativos devem ser considerados.
	 * @return veiculo encontrado.
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Buscar veículo por identificador")
	public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable UUID id,
			@RequestParam(defaultValue = "false") @Parameter(
					description = "Indica se veículos inativos devem ser considerados") boolean incluirInativos) {
		Veiculo veiculo = buscarVeiculoPorIdUseCase.executar(id, incluirInativos)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado com o ID informado."));
		return ResponseEntity.ok(toResponse(veiculo));
	}

	/**
	 * Busca um veiculo pela placa.
	 * @param placa placa informada.
	 * @param incluirInativos indica se veiculos inativos devem ser considerados.
	 * @return veiculo encontrado.
	 */
	@GetMapping("/placa/{placa}")
	@Operation(summary = "Buscar veículo por placa")
	public ResponseEntity<VeiculoResponse> buscarPorPlaca(@PathVariable String placa,
			@RequestParam(defaultValue = "false") @Parameter(
					description = "Indica se veículos inativos devem ser considerados") boolean incluirInativos) {
		Veiculo veiculo = buscarVeiculoPorPlacaUseCase.executar(placa, incluirInativos)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado com a placa informada."));
		return ResponseEntity.ok(toResponse(veiculo));
	}

	/**
	 * Lista todos os veiculos com filtros opcionais.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho solicitado.
	 * @param placa placa opcional.
	 * @param clienteId cliente opcional.
	 * @param incluirInativos indica se veiculos inativos devem ser considerados.
	 * @return pagina de veiculos.
	 */
	@GetMapping
	@Operation(summary = "Listar veículos com paginação e filtros")
	public ResponseEntity<PaginaVeiculosResponse> listar(@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanho, @RequestParam(required = false) String placa,
			@RequestParam(required = false) UUID clienteId,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		PaginaResultado<Veiculo> resultado = listarVeiculosUseCase.executar(pagina, tamanho, placa, clienteId,
				incluirInativos);
		return ResponseEntity.ok(PaginaVeiculosResponse.builder()
			.conteudo(resultado.itens().stream().map(this::toResponse).toList())
			.pagina(resultado.pagina())
			.tamanho(resultado.tamanho())
			.totalElementos(resultado.totalElementos())
			.totalPaginas(resultado.totalPaginas())
			.build());
	}

	/**
	 * Lista todos os veiculos vinculados a um cliente.
	 * @param clienteId identificador do cliente.
	 * @param incluirInativos indica se veiculos inativos devem ser considerados.
	 * @return lista de veiculos encontrados.
	 */
	@GetMapping("/cliente/{clienteId}")
	@Operation(summary = "Listar veículos por cliente")
	public ResponseEntity<List<VeiculoResponse>> listarPorCliente(@PathVariable UUID clienteId,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		List<VeiculoResponse> respostas = listarVeiculosPorClienteUseCase.executar(clienteId, incluirInativos)
			.stream()
			.map(this::toResponse)
			.toList();
		return ResponseEntity.ok(respostas);
	}

	/**
	 * Remove logicamente um veiculo.
	 * @param id identificador do veiculo.
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Remover veículo logicamente")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable UUID id) {
		removerVeiculoUseCase.executar(id);
	}

	private VeiculoResponse toResponse(Veiculo veiculo) {
		return VeiculoResponse.builder()
			.id(veiculo.getId())
			.placa(veiculo.getPlaca().mascarada())
			.marca(veiculo.getMarca())
			.modelo(veiculo.getModelo())
			.ano(veiculo.getAno())
			.cor(veiculo.getCor())
			.observacoes(veiculo.getObservacoes())
			.clientes(montarClientes(veiculo))
			.ativo(veiculo.isAtivo())
			.dataCriacao(veiculo.getDataCriacao())
			.dataUltimaAtualizacao(veiculo.getDataUltimaAtualizacao())
			.dataRemocao(veiculo.getDataRemocao())
			.build();
	}

	private List<ClienteVinculadoResponse> montarClientes(Veiculo veiculo) {
		return veiculo.getClientesVinculados()
			.stream()
			.map(clienteId -> buscarClientePorIdUseCase.executar(clienteId, true)
				.map(cliente -> ClienteVinculadoResponse.builder()
					.id(cliente.getId())
					.nome(cliente.getNome())
					.documentoMascarado(cliente.getDocumento().mascarado())
					.build())
				.orElseThrow(
						() -> new RecursoNaoEncontradoException("Cliente vinculado não encontrado para o veículo.")))
			.sorted(Comparator.comparing(ClienteVinculadoResponse::getNome))
			.toList();
	}

}
