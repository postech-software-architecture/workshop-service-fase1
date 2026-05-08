package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.CadastroServicoRequest;
import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.LoginRequest;
import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests para os endpoints de leitura usados por clientes autenticados
 * (ROLE_CLIENTE). Usa login JWT real porque as rotas dependem do
 * UsuarioAutenticadoPrincipal carregado pelo filtro JWT.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OrdemServicoClienteIT extends PostgresTestContainer {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void limparDados() {
		jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens, usuarios_roles, usuarios, "
				+ "orcamentos_itens, orcamentos, ordens_servico_itens, ordens_servico, servicos, "
				+ "pecas_insumos, estoques, movimentacoes_estoque, veiculos_clientes, veiculos, clientes "
				+ "RESTART IDENTITY CASCADE");
	}

	@Test
	void deveListarApenasOrdensDoClienteAutenticado() throws Exception {
		Cliente cliente = clienteRepository
			.salvar(new Cliente(null, "Cliente Logado", new Documento("12345678909"), "logado@teste.com", null));
		Cliente outroCliente = clienteRepository
			.salvar(new Cliente(null, "Outro Cliente", new Documento("98765432100"), "outro@teste.com", null));
		registrarUsuarioCliente("cliente1", cliente.getId());

		// duas OS para o cliente logado
		criarOsViaApi(cliente.getDocumento().getValor(), "ABC1A11");
		criarOsViaApi(cliente.getDocumento().getValor(), "ABC1A22");
		// uma para outro cliente
		criarOsViaApi(outroCliente.getDocumento().getValor(), "ZZZ9Z99");

		String token = fazerLogin("cliente1", "senha123");

		mockMvc.perform(get("/api/v1/ordens-servico/minhas").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElementos").value(2))
			.andExpect(jsonPath("$.conteudo[0].idCliente").value(cliente.getId().toString()))
			.andExpect(jsonPath("$.conteudo[1].idCliente").value(cliente.getId().toString()));
	}

	@Test
	void deveRejeitarMinhasComForbiddenQuandoUsuarioSemClienteVinculado() throws Exception {
		usuarioRepository.salvar(new Usuario("atendente", "atendente@teste.com", passwordEncoder.encode("senha123"),
				Set.of(Role.ATENDENTE), null));
		String token = fazerLogin("atendente", "senha123");

		mockMvc.perform(get("/api/v1/ordens-servico/minhas").header("Authorization", "Bearer " + token))
			.andExpect(status().isForbidden());
	}

	@Test
	void deveRetornarStatusDaOsQuandoPertenceAoCliente() throws Exception {
		Cliente cliente = clienteRepository
			.salvar(new Cliente(null, "Cliente Status", new Documento("12345678909"), "status@teste.com", null));
		registrarUsuarioCliente("cliente1", cliente.getId());

		UUID osId = criarOsViaApi(cliente.getDocumento().getValor(), "STA1T11");

		String token = fazerLogin("cliente1", "senha123");

		mockMvc.perform(get("/api/v1/ordens-servico/{id}/status", osId).header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(osId.toString()))
			.andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.startsWith("OS-")))
			.andExpect(jsonPath("$.status").value("AGUARDANDO_RESPOSTA_CLIENTE"));
	}

	@Test
	void deveRetornar403QuandoOsPertenceAOutroCliente() throws Exception {
		Cliente cliente = clienteRepository
			.salvar(new Cliente(null, "Cliente Logado", new Documento("12345678909"), "log@teste.com", null));
		Cliente outro = clienteRepository
			.salvar(new Cliente(null, "Outro Cliente", new Documento("98765432100"), "outro@teste.com", null));
		registrarUsuarioCliente("cliente1", cliente.getId());

		UUID osDeOutro = criarOsViaApi(outro.getDocumento().getValor(), "OUT1R11");

		String token = fazerLogin("cliente1", "senha123");

		mockMvc.perform(get("/api/v1/ordens-servico/{id}/status", osDeOutro).header("Authorization", "Bearer " + token))
			.andExpect(status().isForbidden());
	}

	@Test
	void deveRetornar404QuandoOsNaoExiste() throws Exception {
		Cliente cliente = clienteRepository
			.salvar(new Cliente(null, "Cliente Logado", new Documento("12345678909"), "log@teste.com", null));
		registrarUsuarioCliente("cliente1", cliente.getId());

		String token = fazerLogin("cliente1", "senha123");

		mockMvc.perform(
				get("/api/v1/ordens-servico/{id}/status", UUID.randomUUID()).header("Authorization", "Bearer " + token))
			.andExpect(status().isNotFound());
	}

	// --- helpers ---

	private void registrarUsuarioCliente(String username, UUID clienteId) {
		usuarioRepository.salvar(new Usuario(username, username + "@teste.com", passwordEncoder.encode("senha123"),
				Set.of(Role.CLIENTE), clienteId));
	}

	private String fazerLogin(String username, String senha) throws Exception {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(senha);
		MvcResult result = mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
	}

	private UUID criarOsViaApi(String clienteDocumento, String placa) throws Exception {
		UUID servicoId = criarServico("Servico " + placa, new BigDecimal("100.00"));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento(clienteDocumento)
			.veiculoPlaca(placa)
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder().marca("VW").modelo("Gol").ano(2020).build())
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.build();

		MvcResult result = mockMvc
			.perform(post("/api/v1/ordens-servico").with(adminUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
		return UUID.fromString(body.get("id").asText());
	}

	private UUID criarServico(String nome, BigDecimal valor) throws Exception {
		CadastroServicoRequest req = CadastroServicoRequest.builder()
			.nome(nome)
			.descricao(nome + " desc")
			.valor(valor)
			.build();
		MvcResult result = mockMvc
			.perform(post("/api/v1/servicos").with(adminUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isCreated())
			.andReturn();
		return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
	}

	private static org.springframework.test.web.servlet.request.RequestPostProcessor adminUser() {
		return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin")
			.roles("ADMINISTRADOR", "ATENDENTE");
	}

}
