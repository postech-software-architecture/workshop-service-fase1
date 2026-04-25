<!-- Sync Impact Report
Version: 1.0.0
Modified Principles: All
Added Sections: 1. Idioma e Plataforma, 2. Padrões de Código, 3. Arquitetura, 4. Domínio (DDD), 5. Documentação, 6. Testes, 7. Validação e Padrões de Erro (HTTP)
Removed Sections: Core Principles (placeholder)
Templates Updated:
- .specify/templates/plan-template.md (✅ updated)
- .specify/templates/spec-template.md (✅ no changes needed)
- .specify/templates/tasks-template.md (✅ updated)
Follow-up TODOs: None
-->
# workshop-service Constitution

## Core Principles

### 1. Idioma e Plataforma
- Todo código-fonte (nomes de classes, métodos, variáveis, comentários e mensagens internas) deve ser escrito em **Português (pt-BR)**.
- O projeto deve utilizar **Java 25** como versão padrão da linguagem.

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

## Governance

- All PRs/reviews must verify compliance with the principles.
- Amendments require documentation, approval, and a migration plan.
- Use this constitution for runtime development guidance.

**Version**: 1.0.0 | **Ratified**: 2026-04-24 | **Last Amended**: 2026-04-24
