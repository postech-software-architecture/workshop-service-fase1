package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.*;
import com.postech.workshop_service.application.usecases.*;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Endereco;
import com.postech.workshop_service.domain.valueobjects.Documento;
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
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gerenciamento de clientes da oficina")
public class ClienteController {

	private final CriarClienteUseCase criarClienteUseCase;

	private final AtualizarClienteUseCase atualizarClienteUseCase;

	private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;

	private final BuscarClientePorDocumentoUseCase buscarClientePorDocumentoUseCase;

	private final ListarClientesUseCase listarClientesUseCase;

	private final RemoverClienteUseCase removerClienteUseCase;

	/**
	 * Construtor para injeção de dependências.
	 * @param criarClienteUseCase caso de uso de criação.
	 * @param atualizarClienteUseCase caso de uso de atualização.
	 * @param buscarClientePorIdUseCase caso de uso de busca por ID.
	 * @param buscarClientePorDocumentoUseCase caso de uso de busca por documento.
	 * @param listarClientesUseCase caso de uso de listagem.
	 * @param removerClienteUseCase caso de uso de remoção.
	 */
	public ClienteController(CriarClienteUseCase criarClienteUseCase, AtualizarClienteUseCase atualizarClienteUseCase,
			BuscarClientePorIdUseCase buscarClientePorIdUseCase,
			BuscarClientePorDocumentoUseCase buscarClientePorDocumentoUseCase,
			ListarClientesUseCase listarClientesUseCase, RemoverClienteUseCase removerClienteUseCase) {
		this.criarClienteUseCase = criarClienteUseCase;
		this.atualizarClienteUseCase = atualizarClienteUseCase;
		this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
		this.buscarClientePorDocumentoUseCase = buscarClientePorDocumentoUseCase;
		this.listarClientesUseCase = listarClientesUseCase;
		this.removerClienteUseCase = removerClienteUseCase;
	}

	/**
	 * Endpoint para cadastrar um novo cliente.
	 * @param request dados do cliente a ser cadastrado.
	 * @return resposta com os dados do cliente criado e status 201.
	 */
	@PostMapping
	@Operation(summary = "Criar um novo cliente")
	public ResponseEntity<ClienteResponse> criar(@RequestBody @Valid CadastroClienteRequest request) {
		Cliente cliente = criarClienteUseCase.executar(request.getNome(), request.getDocumento(), request.getEmail(),
				request.getTelefone());
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cliente));
	}

	/**
	 * Endpoint para atualizar os dados de um cliente existente.
	 * @param id identificador único do cliente.
	 * @param request novos dados do cliente.
	 * @return resposta com os dados atualizados.
	 */
	@PutMapping("/{id}")
	@Operation(summary = "Atualizar dados de um cliente")
	public ResponseEntity<ClienteResponse> atualizar(@PathVariable UUID id,
			@RequestBody @Valid AtualizarClienteRequest request) {
		Cliente cliente = atualizarClienteUseCase.executar(id, request.getNome(), request.getEmail(),
				request.getTelefone(), toDomain(request.getEndereco()), request.getDataNascimentoFundacao(),
				request.getObservacoes());
		return ResponseEntity.ok(toResponse(cliente));
	}

	/**
	 * Endpoint para buscar um cliente pelo seu identificador único.
	 * @param id identificador único.
	 * @return dados do cliente ou 404 caso não encontrado.
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Buscar cliente por ID")
	public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable UUID id) {
		return buscarClientePorIdUseCase.executar(id)
			.map(cliente -> ResponseEntity.ok(toResponse(cliente)))
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint para buscar um cliente pelo número do documento.
	 * @param documento CPF ou CNPJ.
	 * @return dados do cliente ou 404 caso não encontrado.
	 */
	@GetMapping("/documento/{documento}")
	@Operation(summary = "Buscar cliente por CPF ou CNPJ")
	public ResponseEntity<ClienteResponse> buscarPorDocumento(@PathVariable String documento) {
		return buscarClientePorDocumentoUseCase.executar(documento)
			.map(cliente -> ResponseEntity.ok(toResponse(cliente)))
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint para listar todos os clientes de forma paginada.
	 * @param pagina número da página.
	 * @param tamanho registros por página.
	 * @return lista paginada de clientes.
	 */
	@GetMapping
	@Operation(summary = "Listar clientes com paginação")
	public ResponseEntity<List<ClienteResponse>> listar(@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanho) {
		List<ClienteResponse> lista = listarClientesUseCase.executar(pagina, tamanho)
			.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
		return ResponseEntity.ok(lista);
	}

	/**
	 * Endpoint para remover um cliente do sistema.
	 * @param id identificador único.
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Remover um cliente")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable UUID id) {
		removerClienteUseCase.executar(id);
	}

	private ClienteResponse toResponse(Cliente cliente) {
		EnderecoDTO enderecoDTO = null;
		if (cliente.getEndereco() != null) {
			enderecoDTO = EnderecoDTO.builder()
				.logradouro(cliente.getEndereco().getLogradouro())
				.numero(cliente.getEndereco().getNumero())
				.complemento(cliente.getEndereco().getComplemento())
				.bairro(cliente.getEndereco().getBairro())
				.cidade(cliente.getEndereco().getCidade())
				.estado(cliente.getEndereco().getEstado())
				.cep(cliente.getEndereco().getCep())
				.build();
		}

		return ClienteResponse.builder()
			.id(cliente.getId())
			.nome(cliente.getNome())
			.documento(cliente.getDocumento().mascarado())
			.email(cliente.getEmail())
			.telefone(cliente.getTelefone())
			.endereco(enderecoDTO)
			.dataNascimentoFundacao(cliente.getDataNascimentoFundacao())
			.observacoes(cliente.getObservacoes())
			.dataCriacao(cliente.getDataCriacao())
			.dataUltimaAtualizacao(cliente.getDataUltimaAtualizacao())
			.dataRemocao(cliente.getDataRemocao())
			.build();
	}

	private Endereco toDomain(EnderecoDTO dto) {
		if (dto == null)
			return null;
		return new Endereco(dto.getLogradouro(), dto.getNumero(), dto.getComplemento(), dto.getBairro(),
				dto.getCidade(), dto.getEstado(), dto.getCep());
	}

}
