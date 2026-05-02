package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.AtualizarClienteRequest;
import com.postech.workshop_service.api.dtos.CadastroClienteRequest;
import com.postech.workshop_service.api.dtos.ClienteResponse;
import com.postech.workshop_service.api.dtos.EnderecoDTO;
import com.postech.workshop_service.application.usecases.AtualizarClienteUseCase;
import com.postech.workshop_service.application.usecases.BuscarClientePorDocumentoUseCase;
import com.postech.workshop_service.application.usecases.BuscarClientePorIdUseCase;
import com.postech.workshop_service.application.usecases.BuscarUsuarioAutenticadoUseCase;
import com.postech.workshop_service.application.usecases.CriarClienteUseCase;
import com.postech.workshop_service.application.usecases.ListarClientesUseCase;
import com.postech.workshop_service.application.usecases.RemoverClienteUseCase;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Endereco;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller responsavel pelas operacoes de clientes.
 */
@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gerenciamento de clientes da oficina")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

	private final CriarClienteUseCase criarClienteUseCase;

	private final AtualizarClienteUseCase atualizarClienteUseCase;

	private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;

	private final BuscarClientePorDocumentoUseCase buscarClientePorDocumentoUseCase;

	private final ListarClientesUseCase listarClientesUseCase;

	private final RemoverClienteUseCase removerClienteUseCase;

	private final BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param criarClienteUseCase caso de uso de criacao.
	 * @param atualizarClienteUseCase caso de uso de atualizacao.
	 * @param buscarClientePorIdUseCase caso de uso de busca por ID.
	 * @param buscarClientePorDocumentoUseCase caso de uso de busca por documento.
	 * @param listarClientesUseCase caso de uso de listagem.
	 * @param removerClienteUseCase caso de uso de remocao.
	 * @param buscarUsuarioAutenticadoUseCase caso de uso da identidade autenticada.
	 */
	public ClienteController(CriarClienteUseCase criarClienteUseCase, AtualizarClienteUseCase atualizarClienteUseCase,
			BuscarClientePorIdUseCase buscarClientePorIdUseCase,
			BuscarClientePorDocumentoUseCase buscarClientePorDocumentoUseCase,
			ListarClientesUseCase listarClientesUseCase, RemoverClienteUseCase removerClienteUseCase,
			BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase) {
		this.criarClienteUseCase = criarClienteUseCase;
		this.atualizarClienteUseCase = atualizarClienteUseCase;
		this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
		this.buscarClientePorDocumentoUseCase = buscarClientePorDocumentoUseCase;
		this.listarClientesUseCase = listarClientesUseCase;
		this.removerClienteUseCase = removerClienteUseCase;
		this.buscarUsuarioAutenticadoUseCase = buscarUsuarioAutenticadoUseCase;
	}

	/**
	 * Endpoint para cadastrar um novo cliente.
	 * @param request dados do cliente a ser cadastrado.
	 * @return resposta com os dados do cliente criado e status 201.
	 */
	@PostMapping
	@Operation(summary = "Criar um novo cliente")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	public ResponseEntity<ClienteResponse> criar(@RequestBody @Valid CadastroClienteRequest request) {
		Cliente cliente = criarClienteUseCase.executar(request.getNome(), request.getDocumento(), request.getEmail(),
				request.getTelefone(), toDomain(request.getEndereco()), request.getDataNascimentoFundacao(),
				request.getObservacoes());
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cliente));
	}

	/**
	 * Endpoint para atualizar os dados de um cliente existente.
	 * @param id identificador unico do cliente.
	 * @param request novos dados do cliente.
	 * @return resposta com os dados atualizados.
	 */
	@PutMapping("/{id}")
	@Operation(summary = "Atualizar dados de um cliente")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	public ResponseEntity<ClienteResponse> atualizar(@PathVariable UUID id,
			@RequestBody @Valid AtualizarClienteRequest request) {
		Cliente cliente = atualizarClienteUseCase.executar(id, request.getNome(), request.getEmail(),
				request.getTelefone(), toDomain(request.getEndereco()), request.getDataNascimentoFundacao(),
				request.getObservacoes());
		return ResponseEntity.ok(toResponse(cliente));
	}

	/**
	 * Endpoint para buscar um cliente pelo seu identificador unico.
	 * @param id identificador unico.
	 * @return dados do cliente ou 404 caso nao encontrado.
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Buscar cliente por ID")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable UUID id,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		return buscarClientePorIdUseCase.executar(id, incluirInativos)
			.map(cliente -> ResponseEntity.ok(toResponse(cliente)))
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint para buscar um cliente pelo numero do documento.
	 * @param documento CPF ou CNPJ.
	 * @return dados do cliente ou 404 caso nao encontrado.
	 */
	@GetMapping("/documento/{documento}")
	@Operation(summary = "Buscar cliente por CPF ou CNPJ")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	public ResponseEntity<ClienteResponse> buscarPorDocumento(@PathVariable String documento,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		return buscarClientePorDocumentoUseCase.executar(documento, incluirInativos)
			.map(cliente -> ResponseEntity.ok(toResponse(cliente)))
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint para listar todos os clientes de forma paginada.
	 * @param pagina numero da pagina.
	 * @param tamanho registros por pagina.
	 * @return lista paginada de clientes.
	 */
	@GetMapping
	@Operation(summary = "Listar clientes com paginacao")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
	public ResponseEntity<List<ClienteResponse>> listar(@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamanho,
			@RequestParam(defaultValue = "false") boolean incluirInativos) {
		List<ClienteResponse> lista = listarClientesUseCase.executar(pagina, tamanho, incluirInativos)
			.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
		return ResponseEntity.ok(lista);
	}

	/**
	 * Endpoint para retornar o proprio cadastro do cliente autenticado.
	 * @return resposta com os dados do cliente autenticado.
	 */
	@GetMapping("/me")
	@Operation(summary = "Consultar o proprio cadastro do cliente autenticado")
	@PreAuthorize("hasRole('CLIENTE')")
	public ResponseEntity<ClienteResponse> buscarMeuCadastro() {
		UUID clienteId = buscarUsuarioAutenticadoUseCase.obterClienteIdObrigatorio();
		return buscarClientePorIdUseCase.executar(clienteId, true)
			.map(cliente -> ResponseEntity.ok(toResponse(cliente)))
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint para remover um cliente do sistema.
	 * @param id identificador unico.
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Remover um cliente")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
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
			.ativo(cliente.isAtivo())
			.dataCriacao(cliente.getDataCriacao())
			.dataUltimaAtualizacao(cliente.getDataUltimaAtualizacao())
			.dataRemocao(cliente.getDataRemocao())
			.build();
	}

	private Endereco toDomain(EnderecoDTO dto) {
		if (dto == null) {
			return null;
		}
		return new Endereco(dto.getLogradouro(), dto.getNumero(), dto.getComplemento(), dto.getBairro(),
				dto.getCidade(), dto.getEstado(), dto.getCep());
	}

}
