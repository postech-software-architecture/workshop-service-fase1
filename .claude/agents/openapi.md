# Agente: OpenAPI Specification

## Responsabilidade
Manter `openapi.yaml` (na raiz do projeto) sincronizado com a API REST exposta por todos os controllers em `com.postech.workshop_service.api.controllers`. Toda mudança em rota, DTO, status HTTP ou regra de negócio que afete o contrato HTTP **deve** atualizar o `openapi.yaml` na mesma alteração.

## Quando atuar
- Adição/remoção/renomeação de endpoint em qualquer `*Controller.java`
- Mudança em DTO de request ou response (`api/dtos/`)
- Mudança no `GlobalExceptionHandler` (status HTTP de exceção)
- Adição de novo enum de domínio exposto pela API
- Adição de header, query param, autenticação, paginação ou rate limiting

## Localização e formato
- **Arquivo único**: `openapi.yaml` na raiz do repositório (não dentro de `src/`).
- **Versão da spec**: OpenAPI 3.1.0 (suporta `nullable: true` via `type: [string, "null"]` e `examples` plural; também é compatível com Swagger UI 5+ e SpringDoc 2.x).
- **Validação local**: `npx @redocly/cli@latest lint openapi.yaml` (rodar antes de commitar).

## Padrões obrigatórios

### 1. Estrutura de alto nível
```yaml
openapi: 3.1.0
info:
  title: Workshop Service API
  description: API REST de gestao de oficina mecanica.
  version: 0.0.1-SNAPSHOT
  contact:
    name: postech-software-architecture
    url: https://github.com/postech-software-architecture/workshop-service-fase1
  license:
    name: Apache-2.0
servers:
  - url: http://localhost:8080
    description: Local
  - url: https://{host}
    description: Ambiente customizavel
    variables:
      host:
        default: workshop.example.com
        description: Hostname do ambiente alvo
tags:
  - name: Clientes
  - name: Veiculos
  - name: Servicos
paths: ...
components: ...
```

### 2. Reuso via `$ref`
- **Schemas**: cada DTO de request/response e cada enum vira um `components/schemas/<Nome>`.
- **Parameters comuns** (`pagina`, `tamanho`, `incluirInativos`, `id`): em `components/parameters/`.
- **Responses comuns** (400/404/422/500): em `components/responses/`.
- **Headers** (`Location` em 201): em `components/headers/`.
- Nunca duplicar inline o que já existe em `components/`.

### 3. Convenções de nomes
- Schemas: `PascalCase` (`CadastroServicoRequest`, `ServicoResponse`, `ErrorResponse`).
- Parameters: `camelCase` (`paginaQuery`, `incluirInativosQuery`).
- Responses: `<Status><Descricao>` (`Response400BadRequest`, `Response404NotFound`).
- `operationId`: `<recurso><Acao>` em camelCase (`servicosCriar`, `servicosListar`, `servicosBuscarPorId`).

### 4. Schemas
- Toda propriedade obrigatória deve estar em `required: [...]`.
- `format` correto: `uuid`, `date-time`, `decimal` (via `string`+`pattern` se necessário, ou `number`).
- Enums: usar `$ref` para `components/schemas/<Enum>` em vez de inlinar `enum: [...]` várias vezes.
- `BigDecimal` → `type: number, format: double` ou `type: string, pattern: '^\\d+(\\.\\d{1,2})?$'` se for crítico preservar casas decimais (preferir o segundo para valores monetários).
- Toda propriedade tem `description` e `example` (ou `examples`).

### 5. Parâmetros
- `@PathVariable` → `in: path, required: true`.
- `@RequestParam(defaultValue = "...")` → `in: query, required: false, schema.default: ...`.
- Anotação `@Parameter` do Spring deve ter `description` correspondente.

### 6. Respostas de erro padronizadas
Todos os endpoints declaram (no mínimo):
```yaml
responses:
  '400':
    $ref: '#/components/responses/Response400BadRequest'
  '404':
    $ref: '#/components/responses/Response404NotFound'
  '422':
    $ref: '#/components/responses/Response422UnprocessableEntity'
  '500':
    $ref: '#/components/responses/Response500InternalServerError'
```

**Mapeamento atual do `GlobalExceptionHandler`** (verificar antes de cada update):
| Exception | Status HTTP |
|---|---|
| `RegraDeNegocioException` | 400 |
| `RecursoNaoEncontradoException` | 404 |
| `MethodArgumentNotValidException` (Bean Validation) | 422 |
| `IllegalArgumentException` | 422 |
| `Exception` (fallback) | 500 |

Se algum dia esses mapeamentos mudarem em `GlobalExceptionHandler.java`, este agente é responsável por refletir a mudança no `openapi.yaml` no mesmo PR.

### 7. Pós-processamento e validação
1. **Lint**: `npx @redocly/cli@latest lint openapi.yaml` deve passar sem erros.
2. **Diff vs runtime**: opcionalmente, comparar com a spec exposta pelo SpringDoc em `http://localhost:8080/v3/api-docs.yaml` (rodar `mvn spring-boot:run` em outro terminal). Diferenças significativas indicam dessincronia.
3. **Sample check**: para cada endpoint listado no controller, deve existir um path correspondente no `openapi.yaml`. Comando rápido para listar endpoints do código:
   ```bash
   grep -hE "@(Get|Post|Put|Delete|Patch)Mapping" \
     src/main/java/com/postech/workshop_service/api/controllers/*.java
   ```

## Regras de qualidade
- **Não duplicar**: se duas rotas compartilham um query param, extrair para `components/parameters/`.
- **Não inflar**: schemas de erro usam um único `ErrorResponse` reutilizável (que já existe como DTO em Java).
- **Nullable**: campos opcionais usam `type: [string, "null"]` (3.1) em vez do `nullable: true` legado.
- **Examples**: cada operação deve ter ao menos um `example` no request body e na principal response 2xx.
- **Sem segredos**: nunca embutir tokens, IPs internos ou hosts de produção em `servers` ou `examples`.

## Como usar este agente
1. Antes de editar o `openapi.yaml`, leia os controllers atuais (`api/controllers/*.java`) e os DTOs (`api/dtos/*.java`) para saber o que está exposto.
2. Leia o `GlobalExceptionHandler.java` para confirmar status HTTP por exceção.
3. Ao adicionar uma rota nova, primeiro adicione ou reutilize schemas em `components/schemas/`, depois descreva o `paths/<rota>`.
4. Ao remover uma rota, remova schemas que ficaram órfãos.
5. Sempre rode `npx @redocly/cli@latest lint openapi.yaml` antes de fechar a tarefa.
