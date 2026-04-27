<!-- Sync Impact Report
Version: 1.1.2
Modified Principles: 8. Banco de Dados e Migrations (Fixed Flyway pattern to use dot after V0)
Added Sections: None
Removed Sections: None
Templates Updated:
- .specify/templates/plan-template.md (✅ updated)
- .specify/templates/spec-template.md (✅ no changes needed)
- .specify/templates/tasks-template.md (✅ no changes needed)
Follow-up TODOs: None
-->
# workshop-service Constitution

## Core Principles

### 1. Idioma e Plataforma
- Todo código-fonte (nomes de classes, métodos, variáveis, comentários e mensagens internas) deve ser escrito em **Português (pt-BR)**.
- Exceções pontuais de sufixos técnicos consolidados em inglês são permitidas quando seguirem padrão arquitetural amplamente adotado e não comprometerem a leitura do domínio, como `UseCase`.
- O projeto deve utilizar **Java 21** como versão padrão da linguagem.

### 2. Padrões de Código
- Deve-se utilizar **Lombok** para redução de código boilerplate (`getters`, `constructors`, `equals`, `hashCode`, etc.).
- Não devem ser implementados manualmente `getters` e `setters`, salvo quando houver necessidade explícita de lógica adicional.

**⚠ Ponto de Atenção — Domínio**
- No **domínio**, o uso de Lombok deve respeitar o encapsulamento.
- Evitar o uso indiscriminado de `@Setter` em entidades de domínio.
- Entidades devem expor **comportamentos**, não apenas mutadores.
- Alterações de estado devem ocorrer por métodos que expressem intenção de negócio, e não por setters genéricos.

### 3. Arquitetura
- A solução deve seguir **arquitetura em camadas**, com responsabilidades bem definidas e dependências direcionais:
    - **API / Interface (Controllers)**: expor endpoints e traduzir entrada/saída.
    - **Aplicação (Casos de Uso)**: orquestrar fluxos e coordenar dependências.
    - **Domínio**: concentrar regras e invariantes de negócio.
    - **Infraestrutura**: persistência e integrações externas.
- Controllers não devem conter regras de negócio.
- Regras de negócio não devem residir em DTOs ou na camada de persistência.
- Camadas superiores podem depender das inferiores, nunca o contrário.
- O domínio não deve depender de detalhes de infraestrutura.

### 4. Domínio (DDD)
- O sistema deve seguir princípios de **DDD**.
- O domínio deve ser **rico (não anêmico)**.
- Entidades e Value Objects devem encapsular **invariantes e comportamento**.
- Regras de negócio devem residir no domínio, com orquestração na camada de aplicação.

### 5. Documentação
- Todo método `public` deve possuir **Javadoc**, descrevendo: objetivo, parâmetros, retorno, exceções relevantes.
- Todo endpoint deve possuir **documentação de contrato** (ex.: OpenAPI), incluindo exemplos de request e response.

### 6. Testes
- Todo método `public` deve ser coberto por testes automatizados.
    - Testes unitários devem cobrir as variações e ramificações relevantes.
    - Testes de integração devem cobrir ao menos: 1 happy path, 1 edge case.
- Métodos que não forem `public` não devem ser testados diretamente; devem ser validados por meio do comportamento exposto por APIs ou métodos `public`.

### 7. Validação e Padrões de Erro (HTTP)
- DTOs de entrada devem declarar obrigatoriedade de campos e tamanhos máximos.
- Violações de validação estrutural devem retornar **HTTP 422**.
- Violações de regra de negócio devem retornar **HTTP 400**.
- Regras de negócio devem ser validadas na camada de aplicação/serviço, não no controller.

### 8. Banco de Dados e Migrations (Flyway)
As migrations devem seguir o padrão cronológico do Flyway:
- **Formato**: `V0.YYYYMMDDHHMMSS__descricao_da_acao.sql`
- **Exemplo**: `V0.20260424213700__create_table_clientes.sql`
- É **obrigatório** que toda tabela e coluna criada possua um comentário explicativo direto no SQL (compatível com PostgreSQL).
- Toda tabela de entidade deve conter os campos de auditoria: `data_criacao` e `data_ultima_atualizacao` (TIMESTAMP/TIMESTAMPTZ, não nulo).
- Utilizar `UUID` como chave primária (`PK`).
- Chaves estrangeiras (`FK`) devem ser nomeadas como `fk_origem_destino`.
- Índices de unicidade (`UNIQUE`) para campos de identificação de negócio.

## Governance

- All PRs/reviews must verify compliance with the principles.
- Amendments require documentation, approval, and a migration plan.
- Use this constitution for runtime development guidance.

**Version**: 1.1.2 | **Ratified**: 2026-04-24 | **Last Amended**: 2026-04-26
