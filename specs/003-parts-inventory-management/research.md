# Research: Gestao de Pecas, Insumos e Estoques

**Feature**: 003-parts-inventory-management  
**Date**: 2026-04-29
**Updated**: 2026-04-29 (nova estrutura de dados com entidade Estoques)

## Research Topics

### 1. Estrutura de Dados para PecaInsumo

**Decision**: Entidade raiz com campos obrigatorios (SKU, nome, quantidade, valor unitario, estoque minimo, unidade) e opcionais (fornecedor, codigo de barras, localizacao, marca, categoria, aplicacao, observacoes).

**Rationale**: 
- SKU como identificador de negocio unico entre pecas ativas
- Categoria como texto livre (nao entidade separada) conforme clarificacao
- Unidade de medida como enumeracao com valores predefinidos

**Alternatives Considered**:
- Categoria como entidade separada com CRUD: rejeitado por simplicidade do MVP
- SKU como chave primaria: rejeitado para manter UUID como padrao do projeto

### 2. Estrutura de Dados para Estoque e MovimentacaoEstoque

**Decision**: Tres entidades em cascata: PecaInsumo (1) → (N) Estoque → (N) MovimentacaoEstoque.

**Rationale**:
- PecaInsumo nao armazena quantidade total (delegado a Estoques)
- Estoque representa quantidade em uma localizacao especifica
- Permite multiplas localizacoes por peca
- MovimentacaoEstoque registra alteracoes em um estoque especifico
- Ajuste como valor absoluto que substitui quantidade do estoque
- Sem campo "responsavel" no MVP (sem autenticacao)

**Alternatives Considered**:
- Quantidade em PecaInsumo: rejeitado para suportar multiplas localizacoes
- Ajuste como delta (+/-): rejeitado; valor absoluto e mais comum para inventarios
- Campo responsavel: rejeitado para MVP, sera adicionado com autenticacao

### 3. Controle de Concorrencia

**Decision**: Optimistic locking com campo `versao` na entidade PecaInsumo.

**Rationale**:
- Padrão amplamente adotado em Spring Data JPA
- Evita locks pesados no banco de dados
- Detecta conflito e rejeita operacao concorrente com erro claro
- Usuario pode tentar novamente apos conflito

**Alternatives Considered**:
- Pessimistic locking: rejeitado por overhead e complexidade
- Fila serializada: rejeitado por nao ser necessario na escala do MVP

### 4. Validacao de SKU

**Decision**: SKU unico apenas entre pecas ativas; permite reutilizacao apos soft delete.

**Rationale**:
- Consistente com padrao de placa de veiculos ja implementado
- Permite reativacao do catalogo sem conflitos de SKU
- Query deve ignorar pecas inativas na verificacao de unicidade

**Alternatives Considered**:
- SKU unico global: rejeitado por impedir reutilizacao de codigos de produtos descontinuados

### 5. Validacao de Estoque Negativo

**Decision**: Estoque nunca pode ser negativo; saidas verificam disponibilidade antes de executar.

**Rationale**:
- Regra de negocio critica para controle de inventario
- Validacao na camada de aplicacao antes de decrementar
- Mensagem de erro clara quando tentativa de saida maior que disponivel

**Alternatives Considered**:
- Permitir estoque negativo com reserva: rejeitado por complexidade e risco operacional

### 6. Estrategia de Soft Delete

**Decision**: Campo `ativo` (boolean) e `data_remocao` (timestamp nullable) na PecaInsumo.

**Rationale**:
- Consistente com padrao ja utilizado em Cliente e Veiculo
- Preserva historico de movimentacoes
- Consultas padrao filtram por `ativo = true`

**Alternatives Considered**:
- Exclusao fisica: rejeitado por perder rastreabilidade

### 7. Endpoints REST

**Decision**: Seguir padrao RESTful do projeto com endpoints para CRUD, movimentacoes e consultas especiais.

**Endpoints planejados**:
- `POST /api/v1/pecas` - Cadastrar peca
- `GET /api/v1/pecas` - Listar pecas (paginacao, filtros)
- `GET /api/v1/pecas/{id}` - Buscar por ID
- `GET /api/v1/pecas/sku/{sku}` - Buscar por SKU
- `GET /api/v1/pecas/estoque-baixo` - Listar itens abaixo do minimo
- `PUT /api/v1/pecas/{id}` - Atualizar peca
- `DELETE /api/v1/pecas/{id}` - Remover peca (soft delete)
- `POST /api/v1/pecas/{id}/movimentacoes` - Registrar movimentacao
- `GET /api/v1/pecas/{id}/movimentacoes` - Historico de movimentacoes

**Rationale**:
- Consistente com endpoints de clientes e veiculos
- URLs em kebab-case, parametros em camelCase

### 8. Nome de Tabelas no Banco de Dados

**Decision**: Nomes no plural, seguindo padrao existente do projeto.

**Rationale**:
- Consistente com tabelas existentes: `clientes`, `veiculos`, `veiculos_clientes`
- Padrao amplamente adotado em bancos de dados relacionais
- Facilita leitura e compreensao do schema

**Alternatives Considered**:
- Nomes no singular: rejeitado por quebrar padrao do projeto

### 9. Unidade de Medida

**Decision**: Enumeracao com valores: UN, L, KG, M, ML, CX, PC.

**Rationale**:
- Valores mais comuns em oficinas mecanicas
- Validacao simples via enumeracao
- Extensivel no futuro se necessario

**Alternatives Considered**:
- Tabela de unidades: rejeitado por over-engineering para MVP

## Dependencies

### Existing Project Patterns
- **EntidadeBase**: Classe base com id, datas de auditoria e remocao logica
- **BaseJpaEntity**: Classe base JPA com campos de auditoria
- **GlobalExceptionHandler**: Tratamento de excecoes com 400/422/404
- **MapStruct**: Mapeamento entre dominio e JPA

### New Dependencies
- Nenhuma dependencia nova necessaria; usar stack existente

## Open Questions

Nenhuma. Todas as duvidas foram resolvidas nas sessoes de clarificacao.
