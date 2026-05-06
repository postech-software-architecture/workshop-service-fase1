# Research: Controle de Acesso Autenticado

## Decision 1: Implementar emissao e validacao de JWT com Spring Security e componentes dedicados de token

- **Decision**: Implementar emissao de access token JWT assinado pela aplicacao e validacao em um componente de seguranca integrado ao `SecurityFilterChain`, populando o contexto autenticado antes dos controllers.
- **Rationale**: O projeto ja usa Spring Security; aproveitar a cadeia de filtros atual reduz codigo acoplado, concentra autenticacao HTTP em infraestrutura e preserva os casos de uso como orquestradores de negocio.
- **Alternatives considered**:
  - Criar autenticacao stateful em sessao HTTP: rejeitado por conflitar com o objetivo de API stateless.
  - Liberar tudo no filtro e validar token manualmente em cada controller: rejeitado por duplicar regras e enfraquecer o controle centralizado.

## Decision 2: Modelar usuario de acesso e refresh token como conceitos de dominio separados

- **Decision**: Criar uma entidade de dominio para `Usuario` com identificador, username, senha protegida, estado da conta e papeis; e uma entidade de dominio para `RefreshToken` com expiracao, revogacao e operacoes de ciclo de vida.
- **Rationale**: A feature introduz regras de negocio novas, como revogar renovacao, impedir reutilizacao e traduzir perfis para autorizacao; isso deve ficar expresso no dominio e nao escondido em DTOs ou entidades JPA.
- **Alternatives considered**:
  - Tratar refresh token apenas como tabela tecnica sem dominio: rejeitado porque expira, revoga e participa de regras de negocio.
  - Colocar roles como strings soltas em infraestrutura: rejeitado por perder consistencia com o controle de acesso por perfil.

## Decision 3: Persistir refresh token por sessao de login, com revogacao individual

- **Decision**: Cada login emitira um refresh token persistido independentemente, vinculado a um usuario e revogavel de forma individual no logout ou por invalidacao de seguranca.
- **Rationale**: Isso permite multiplas sessoes simultaneas sem apagar renovacoes legitimas de outros dispositivos e simplifica a regra inicial de logout pedida pelo usuario.
- **Alternatives considered**:
  - Um unico refresh token ativo por usuario: rejeitado porque derruba sessoes paralelas sem necessidade de negocio explicita.
  - Blacklist de access token: rejeitado neste primeiro momento por aumentar complexidade sem ser requisito da feature.

## Decision 4: Aplicar RBAC por `ROLE_` no Spring Security e refinar autorizacao por endpoint

- **Decision**: Persistir os perfis de negocio como `ADMINISTRADOR`, `ATENDENTE`, `MECANICO` e `CLIENTE`, convertendo-os para authorities com prefixo `ROLE_` na autenticacao e aplicando `@PreAuthorize` nos controllers expostos.
- **Rationale**: Isso casa com a necessidade expressa pelo usuario, usa o modelo nativo do Spring e torna a intencao de permissao legivel nos endpoints.
- **Alternatives considered**:
  - Validar permissao dentro dos use cases apenas: rejeitado porque perde a protecao declarativa e torna falhas de acesso menos uniformes.
  - Criar um enum diferente para security e outro para negocio: rejeitado por duplicar fonte de verdade.

## Decision 5: Padronizar respostas de seguranca em 401 e 403 com handlers dedicados

- **Decision**: Configurar um `AuthenticationEntryPoint` para credenciais ausentes/invalidas e um `AccessDeniedHandler` para usuarios autenticados sem permissao, mantendo o `GlobalExceptionHandler` para validacoes e regras de negocio.
- **Rationale**: Isso separa falhas de seguranca das demais excecoes da aplicacao e atende diretamente aos criterios de aceite da feature.
- **Alternatives considered**:
  - Reutilizar o handler generico para tudo: rejeitado porque 401/403 nascem antes do controller e exigem integracao com Spring Security.
  - Responder 200 com payload de erro de autorizacao: rejeitado por quebrar semantica HTTP.

## Decision 6: Adicionar testes unitarios e de integracao focados no fluxo completo de autenticacao

- **Decision**: Cobrir entidades e casos de uso de autenticacao com testes unitarios, e cobrir login, refresh, logout, `/api/auth/me`, 401 e 403 com testes de integracao HTTP usando infraestrutura Spring/Testcontainers ja existente.
- **Rationale**: A feature afeta o acesso a todo o servico; testes apenas unitarios nao capturam filtros, configuracao de seguranca e persistencia do refresh token.
- **Alternatives considered**:
  - Somente testes de controller com mocks: rejeitado por nao validar a cadeia de seguranca real.
  - Somente testes de integracao: rejeitado por deixar regras de dominio e casos de uso com diagnostico ruim.
