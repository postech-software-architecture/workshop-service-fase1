# Agente: API Layer (Controllers e DTOs)

## Responsabilidade
Criar controllers REST, DTOs de request/response e documentação Swagger/OpenAPI.

## Contexto do projeto
- Java 21 + Spring Boot 3.4.1 + SpringDoc OpenAPI 2.8.4 + Lombok + Jakarta Validation
- Pacotes:
  - Controllers: `com.postech.workshop_service.api.controllers`
  - DTOs: `com.postech.workshop_service.api.dtos`
- Caminho: `src/main/java/com/postech/workshop_service/api/`
- Base URL: `/api/v1/`

## Padrões obrigatórios

### DTOs de Request
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dados para cadastro de X")
public class CadastroXRequest {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
    @Schema(example = "Exemplo")
    private String nome;

    @NotNull(message = "O valor e obrigatorio")
    @Positive(message = "O valor deve ser positivo")
    private BigDecimal valor;
}
```

### DTOs de Response
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dados de X")
public class XResponse {
    private UUID id;
    private String nome;
    private boolean ativo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;
    private LocalDateTime dataRemocao;
}
```

### Controller
```java
@RestController
@RequestMapping("/api/v1/xs")
@Tag(name = "Xs", description = "Gerenciamento de Xs")
public class XController {

    // injeção via construtor (sem @Autowired)

    @PostMapping
    @Operation(summary = "Cadastrar X")
    public ResponseEntity<XResponse> criar(@RequestBody @Valid CadastroXRequest request) {
        X x = criarXUseCase.executar(/* parâmetros extraídos do request */);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(x));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover X")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        removerXUseCase.executar(id);
    }

    private XResponse toResponse(X x) {
        return XResponse.builder()
            .id(x.getId())
            // ...
            .build();
    }
}
```

### Códigos HTTP
- `201 Created` → POST de criação
- `200 OK` → GET, PUT
- `204 No Content` → DELETE
- `400 Bad Request` → validação de bean (Bean Validation)
- `404 Not Found` → `RecursoNaoEncontradoException`
- `422 Unprocessable Entity` → `RegraDeNegocioException`

### Paginação
Usar `PaginaXResponse` com campos: `conteudo`, `pagina`, `tamanho`, `totalElementos`, `totalPaginas`

### @RequestParam com @Parameter
```java
@RequestParam(defaultValue = "false")
@Parameter(description = "Incluir registros inativos")
boolean incluirInativos
```

## Como usar este agente
1. Ler `VeiculoController`, `CadastroVeiculoRequest` e `VeiculoResponse` antes de criar novos arquivos
2. Criar os DTOs e o controller juntos
3. O controller converte DTO → parâmetros para o use case, e entidade de domínio → DTO de resposta (método `toResponse`)
4. Não criar use cases, entidades de domínio ou infrastructure
