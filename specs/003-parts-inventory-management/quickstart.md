# Quickstart: Gestao de Pecas, Insumos e Estoques

**Feature**: 003-parts-inventory-management  
**Date**: 2026-04-29

## Pre-requisitos

- Java 21 instalado
- Maven 3.9+
- PostgreSQL rodando (via Docker ou local)
- Projeto base ja configurado (clientes e veiculos)

## Passos de Implementacao

### 1. Criar Migration do Banco de Dados

```bash
# Criar arquivo de migration
touch src/main/resources/db/migration/V0.20260429220000__create_table_pecas_movimentacoes.sql
```

**Conteudo do migration**:
- Tabela `pecas_insumos` com todos os campos definidos no data-model.md
- Tabela `movimentacoes_estoque` com FK para pecas
- Indices para consultas frequentes
- Comentarios em todas as tabelas e colunas

### 2. Criar Value Objects

**Arquivos**:
- `src/main/java/com/postech/workshop_service/domain/valueobjects/TipoMovimentacao.java`
- `src/main/java/com/postech/workshop_service/domain/valueobjects/UnidadeMedida.java`

### 3. Criar Entidades de Dominio

**Arquivos**:
- `src/main/java/com/postech/workshop_service/domain/entities/PecaInsumo.java`
- `src/main/java/com/postech/workshop_service/domain/entities/MovimentacaoEstoque.java`

**Regras**:
- `PecaInsumo` estende `EntidadeBase`
- Implementar metodos de dominio: `registrarEntrada()`, `registrarSaida()`, `ajustarEstoque()`, `removerLogicamente()`
- Validar invariantes: estoque >= 0, valor > 0, SKU obrigatorio

### 4. Criar Interfaces de Repositorio

**Arquivos**:
- `src/main/java/com/postech/workshop_service/domain/repositories/PecaInsumoRepository.java`
- `src/main/java/com/postech/workshop_service/domain/repositories/MovimentacaoEstoqueRepository.java`

### 5. Criar Entidades JPA

**Arquivos**:
- `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/PecaInsumoJpaEntity.java`
- `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/MovimentacaoEstoqueJpaEntity.java`

**Anotacoes importantes**:
- `@Version` para optimistic locking em `PecaInsumoJpaEntity`
- `@OneToMany(mappedBy = "pecaInsumo", cascade = CascadeType.PERSIST)` para movimentacoes

### 6. Criar Mappers (MapStruct)

**Arquivos**:
- `src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/PecaInsumoMapper.java`
- `src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/MovimentacaoEstoqueMapper.java`

### 7. Criar Repositorios JPA

**Arquivos**:
- `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/PecaInsumoJpaRepository.java`
- `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/MovimentacaoEstoqueJpaRepository.java`

**Metodos customizados**:
- `Optional<PecaInsumoJpaEntity> findBySkuAndAtivoTrue(String sku)`
- `List<PecaInsumoJpaEntity> findByAtivoTrueAndQuantidadeEstoqueLessThanEqualEstoqueMinimo()`
- `Page<MovimentacaoEstoqueJpaEntity> findByPecaInsumoIdOrderByDataMovimentacaoDesc(UUID pecaId, Pageable pageable)`

### 8. Criar DTOs

**Arquivos**:
- `src/main/java/com/postech/workshop_service/api/dtos/CadastroPecaRequest.java`
- `src/main/java/com/postech/workshop_service/api/dtos/AtualizarPecaRequest.java`
- `src/main/java/com/postech/workshop_service/api/dtos/PecaResponse.java`
- `src/main/java/com/postech/workshop_service/api/dtos/MovimentacaoRequest.java`
- `src/main/java/com/postech/workshop_service/api/dtos/MovimentacaoResponse.java`

**Validacoes**:
- `@NotBlank` para campos obrigatorios
- `@Positive` para valor unitario
- `@PositiveOrZero` para quantidades e estoque minimo
- `@Size(max = N)` para campos de texto

### 9. Criar Casos de Uso

**Arquivos**:
- `CriarPecaUseCase.java` - Cadastrar nova peca com validacao de SKU unico
- `AtualizarPecaUseCase.java` - Atualizar dados com optimistic locking
- `BuscarPecaPorIdUseCase.java` - Buscar por UUID
- `BuscarPecaPorSkuUseCase.java` - Buscar por SKU
- `ListarPecasUseCase.java` - Listar com paginacao e filtros
- `RemoverPecaUseCase.java` - Soft delete
- `RegistrarMovimentacaoUseCase.java` - Entrada/saida/ajuste com validacoes
- `ListarHistoricoMovimentacoesUseCase.java` - Historico com filtros
- `ListarPecasEstoqueBaixoUseCase.java` - Alertas de estoque baixo

### 10. Criar Controller

**Arquivo**: `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`

**Endpoints**:
- `POST /api/v1/pecas`
- `GET /api/v1/pecas`
- `GET /api/v1/pecas/{id}`
- `GET /api/v1/pecas/sku/{sku}`
- `GET /api/v1/pecas/estoque-baixo`
- `PUT /api/v1/pecas/{id}`
- `DELETE /api/v1/pecas/{id}`
- `POST /api/v1/pecas/{id}/movimentacoes`
- `GET /api/v1/pecas/{id}/movimentacoes`

### 11. Atualizar OpenAPI Principal

Adicionar os endpoints de pecas ao arquivo `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`.

### 12. Criar Testes

**Testes Unitarios**:
- `PecaInsumoTest.java` - Testar regras de dominio
- `MovimentacaoEstoqueTest.java` - Testar tipos de movimentacao
- `*UseCaseTest.java` - Testar cada caso de uso com mocks

**Testes de Integracao**:
- `PecaInsumoControllerIntegrationTest.java` - Testar endpoints via MockMvc
- `PecaInsumoRepositoryIntegrationTest.java` - Testar persistencia com Testcontainers

**Cenarios de teste obrigatorios**:
- Happy path para cada operacao
- SKU duplicado ativo (erro 400)
- Estoque insuficiente para saida (erro 400)
- Conflito de versao/optimistic locking (erro 400)
- Validacao estrutural (erro 422)
- Recurso nao encontrado (erro 404)

## Verificacao

### Executar aplicacao

```bash
./mvnw spring-boot:run
```

### Executar testes

```bash
./mvnw test
```

### Verificar cobertura

```bash
./mvnw jacoco:report
# Abrir target/site/jacoco/index.html
```

### Testar endpoints manualmente

```bash
# Cadastrar peca
curl -X POST http://localhost:8080/api/v1/pecas \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "FILT-001",
    "nome": "Filtro de oleo",
    "quantidadeEstoque": 10,
    "valorUnitario": 45.90,
    "estoqueMinimo": 5,
    "unidadeMedida": "UN"
  }'

# Buscar por SKU
curl http://localhost:8080/api/v1/pecas/sku/FILT-001

# Registrar entrada
curl -X POST http://localhost:8080/api/v1/pecas/{id}/movimentacoes \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "ENTRADA",
    "quantidade": 5,
    "motivo": "Reposicao"
  }'

# Verificar estoque baixo
curl http://localhost:8080/api/v1/pecas/estoque-baixo
```

## Checklist de Conclusao

- [ ] Migration criada e executada com sucesso
- [ ] Value objects implementados
- [ ] Entidades de dominio com regras de negocio
- [ ] Repositorios implementados
- [ ] DTOs com validacoes
- [ ] Casos de uso implementados
- [ ] Controller com todos os endpoints
- [ ] OpenAPI atualizado
- [ ] Testes unitarios com cobertura >= 80%
- [ ] Testes de integracao cobrindo happy path e edge cases
- [ ] Codigo em pt-BR
- [ ] Javadocs em todos os metodos publicos
