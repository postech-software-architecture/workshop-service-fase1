# Agente: Testes

## Responsabilidade
Criar testes unitários para use cases e domínio, e testes de integração para controllers e repositórios.

## Contexto do projeto
- Java 21 + JUnit 5 + Mockito + MockMvc + Testcontainers (PostgreSQL)
- Cobertura mínima exigida: **80% de instruções** (JaCoCo — build falha se não atingir)
- Caminhos:
  - Testes unitários: `src/test/java/com/postech/workshop_service/application/usecases/`
  - Testes de domínio: `src/test/java/com/postech/workshop_service/domain/`
  - Testes de integração (controller): `src/test/java/com/postech/workshop_service/api/controllers/`
  - Testes de integração (repositório): `src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/`

## Classe base para testes de integração

```java
// Estender PostgresTestContainer para testes que precisam de banco
class XControllerIT extends PostgresTestContainer { ... }
class XRepositoryImplIT extends PostgresTestContainer { ... }
```

## Padrão de teste unitário (use case)

```java
@ExtendWith(MockitoExtension.class)
class CriarXUseCaseTest {

    @Mock
    private XRepository xRepository;

    @InjectMocks
    private CriarXUseCase criarXUseCase;

    @Test
    void shouldCreateX() {
        when(xRepository.existeNomeAtivo("Teste", null)).thenReturn(false);
        when(xRepository.salvar(any(X.class))).thenAnswer(inv -> inv.getArgument(0));

        X resultado = criarXUseCase.executar("Teste", /* outros params */);

        assertEquals("Teste", resultado.getNome());
    }

    @Test
    void shouldRejectDuplicateName() {
        when(xRepository.existeNomeAtivo("Teste", null)).thenReturn(true);

        assertThrows(RegraDeNegocioException.class,
            () -> criarXUseCase.executar("Teste", /* outros params */));
    }
}
```

## Padrão de teste de integração (controller)

```java
@AutoConfigureMockMvc
class XControllerIT extends PostgresTestContainer {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldExecuteFullFlow() throws Exception {
        // POST - criar
        MvcResult result = mockMvc.perform(post("/api/v1/xs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Teste"))
            .andReturn();

        UUID id = UUID.fromString(
            objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText());

        // GET by id
        mockMvc.perform(get("/api/v1/xs/{id}", id))
            .andExpect(status().isOk());

        // DELETE
        mockMvc.perform(delete("/api/v1/xs/{id}", id))
            .andExpect(status().isNoContent());

        // GET after delete → 404
        mockMvc.perform(get("/api/v1/xs/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenRequiredFieldMissing() throws Exception { ... }

    @Test
    void shouldReturn422WhenBusinessRuleViolated() throws Exception { ... }
}
```

## O que cobrir obrigatoriamente

### Testes unitários (use cases)
- Caminho feliz (criação, atualização, remoção, busca)
- Duplicidade rejeitada
- Recurso não encontrado
- Validação de campos obrigatórios

### Testes de domínio (entidade)
- Construtor valida campos obrigatórios
- `removerLogicamente()` é idempotente
- Sanitização de strings

### Testes de integração (controller)
- Fluxo completo CRUD em um único teste de fluxo
- Cada validação de request em teste separado
- Regras de negócio retornam 422

## Como usar este agente
1. Ler `CriarVeiculoUseCaseTest` e `VeiculoControllerIT` antes de criar novos testes
2. Criar testes unitários para todos os use cases + testes de domínio + IT de controller + IT de repositório
3. Garantir cobertura dos cenários de erro (não só o caminho feliz)
4. Não criar código de produção
