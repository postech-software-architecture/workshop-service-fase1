# Feature Specification: Controle de Acesso Autenticado

**Feature Branch**: `006-jwt-auth-rbac`  
**Created**: 2026-05-01  
**Status**: Draft  
**Input**: User description: "Implementar autenticação e autorização com JWT, refresh token e controle de acesso por perfil para APIs do sistema de oficina"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Entrar em area protegida (Priority: P1)

Como usuario com cadastro ativo no sistema, quero informar minhas credenciais e receber uma sessao autenticada para acessar apenas as funcoes permitidas ao meu perfil.

**Why this priority**: Sem autenticacao confiavel, nao ha como proteger operacoes administrativas, identificar o usuario nem aplicar regras por perfil.

**Independent Test**: Pode ser testada de forma independente validando acesso bem-sucedido com credenciais corretas, rejeicao com credenciais invalidas e consulta da identidade do usuario autenticado.

**Acceptance Scenarios**:

1. **Given** um usuario ativo com credenciais validas, **When** ele inicia sessao, **Then** o sistema concede uma credencial de acesso com prazo de expiracao e informa o tempo restante da sessao.
2. **Given** um usuario com credenciais invalidas, **When** ele tenta iniciar sessao, **Then** o sistema recusa o acesso sem expor detalhes sensiveis sobre a conta.
3. **Given** uma sessao autenticada valida, **When** o usuario consulta seus dados de autenticacao, **Then** o sistema retorna sua identidade e os perfis associados.

---

### User Story 2 - Renovar ou encerrar sessao (Priority: P2)

Como usuario autenticado, quero renovar minha sessao sem reenviar minhas credenciais enquanto a renovacao ainda for valida, e quero poder encerrar essa capacidade quando sair do sistema.

**Why this priority**: Isso reduz friccao para usuarios legitimos, preserva a seguranca operacional e permite encerrar a continuidade da sessao sem depender de nova autenticacao imediata.

**Independent Test**: Pode ser testada de forma independente concedendo uma credencial de renovacao, renovando a sessao com sucesso, recusando renovacao expirada ou revogada e invalidando a renovacao no encerramento de sessao.

**Acceptance Scenarios**:

1. **Given** uma credencial de renovacao valida e nao revogada, **When** o usuario solicita renovacao da sessao, **Then** o sistema concede nova credencial de acesso sem exigir nova senha.
2. **Given** uma credencial de renovacao expirada ou revogada, **When** o usuario solicita renovacao da sessao, **Then** o sistema recusa a operacao e exige nova autenticacao.
3. **Given** uma credencial de renovacao valida, **When** o usuario encerra sua sessao, **Then** o sistema invalida essa renovacao para impedir novos acessos derivados dela.

---

### User Story 3 - Restringir acoes por perfil (Priority: P3)

Como gestor do sistema, quero que cada perfil de usuario acesse apenas as funcionalidades compativeis com sua responsabilidade para reduzir erros operacionais e acesso indevido.

**Why this priority**: O negocio depende de segregacao clara entre administracao, atendimento, execucao tecnica e consulta pelo cliente.

**Independent Test**: Pode ser testada de forma independente validando que cada perfil consegue executar apenas suas acoes permitidas e recebe bloqueio ao tentar acessar funcionalidade fora do seu escopo.

**Acceptance Scenarios**:

1. **Given** um usuario autenticado com perfil administrativo, **When** ele acessa uma funcionalidade restrita a administradores, **Then** o sistema permite a operacao.
2. **Given** um usuario autenticado sem permissao para determinada funcionalidade, **When** ele tenta executa-la, **Then** o sistema bloqueia a acao por falta de autorizacao.
3. **Given** um cliente autenticado, **When** ele consulta informacoes operacionais vinculadas a sua propria relacao com a oficina, **Then** o sistema mostra apenas dados que lhe pertencem.

### Edge Cases

- Tentativa de autenticacao com conta inativa, removida ou bloqueada deve ser recusada da mesma forma que outras falhas de credencial.
- Reutilizacao de credencial de renovacao apos encerramento de sessao deve ser bloqueada imediatamente.
- Requisicoes a areas protegidas sem credencial de acesso devem ser recusadas sem executar a regra de negocio.
- Requisicoes com sessao autenticada valida, mas sem permissao para a funcionalidade, devem ser recusadas sem alterar dados.
- Um cliente autenticado nao pode acessar dados de outra pessoa mesmo quando o recurso exista.
- Recursos publicos definidos pelo negocio devem continuar acessiveis sem autenticacao.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST autenticar usuarios cadastrados por meio de identificador de acesso e senha.
- **FR-002**: O sistema MUST armazenar segredos de autenticacao em formato nao reversivel.
- **FR-003**: O sistema MUST conceder uma credencial de acesso temporaria somente apos autenticacao bem-sucedida.
- **FR-004**: O sistema MUST conceder, junto com a credencial de acesso, uma credencial de renovacao com prazo maior para continuidade da sessao.
- **FR-005**: O sistema MUST registrar a credencial de renovacao emitida com referencia ao usuario, prazo de expiracao, data de criacao e estado de revogacao.
- **FR-006**: O sistema MUST permitir renovacao de sessao apenas quando a credencial de renovacao existir, estiver dentro do prazo e nao tiver sido revogada.
- **FR-007**: O sistema MUST impedir renovacao de sessao quando a credencial de renovacao estiver expirada, inexistente ou revogada.
- **FR-008**: O sistema MUST permitir encerramento de sessao com invalidacao da credencial de renovacao utilizada.
- **FR-009**: O sistema MUST identificar o usuario autenticado em requisicoes protegidas e disponibilizar sua identidade e perfis para as regras de autorizacao.
- **FR-010**: O sistema MUST manter acessiveis sem autenticacao apenas os recursos explicitamente classificados como publicos pelo negocio.
- **FR-011**: O sistema MUST exigir autenticacao para todos os demais recursos administrativos e operacionais nao classificados como publicos.
- **FR-012**: O sistema MUST aplicar controle de acesso por perfil para os papeis ADMINISTRADOR, ATENDENTE, MECANICO e CLIENTE.
- **FR-013**: O sistema MUST permitir que ADMINISTRADOR acesse todas as funcionalidades cobertas por esta feature.
- **FR-014**: O sistema MUST permitir que ATENDENTE execute cadastro de clientes, cadastro de veiculos, abertura de ordens de servico e consulta de ordens de servico.
- **FR-015**: O sistema MUST impedir que ATENDENTE gerencie usuarios e altere catalogos operacionais restritos a administracao.
- **FR-016**: O sistema MUST permitir que MECANICO consulte ordens de servico atribuidas, atualize o andamento dessas ordens e consulte pecas e insumos.
- **FR-017**: O sistema MUST impedir que MECANICO cadastre clientes, altere catalogos operacionais restritos e gerencie usuarios.
- **FR-018**: O sistema MUST permitir que CLIENTE consulte seus proprios dados e acompanhe o andamento de suas proprias ordens de servico.
- **FR-019**: O sistema MUST permitir que CLIENTE aprove, rejeite ou cancele seu proprio orcamento quando essa etapa fizer parte do fluxo da ordem de servico.
- **FR-020**: O sistema MUST impedir que qualquer usuario autenticado acesse dados ou execute acoes fora das permissoes do seu perfil.
- **FR-021**: O sistema MUST retornar resposta de acesso negado quando o usuario estiver autenticado, mas nao tiver permissao suficiente para a operacao.
- **FR-022**: O sistema MUST retornar resposta de nao autenticado quando a requisicao exigir sessao valida e ela nao estiver presente ou estiver invalida.
- **FR-023**: O sistema MUST disponibilizar uma forma de o usuario autenticado consultar sua propria identidade e os perfis associados a sua sessao atual.

### Key Entities *(include if feature involves data)*

- **Usuario de Acesso**: Representa a pessoa habilitada a usar o sistema, com identificador de acesso, segredo autenticavel, estado da conta e um ou mais perfis autorizados.
- **Perfil de Acesso**: Representa a responsabilidade de negocio atribuida ao usuario, definindo o conjunto de funcionalidades permitidas para ADMINISTRADOR, ATENDENTE, MECANICO e CLIENTE.
- **Sessao de Acesso**: Representa a concessao temporaria de uso do sistema apos autenticacao bem-sucedida, vinculada a um usuario autenticado e a seus perfis.
- **Credencial de Renovacao**: Representa a autorizacao persistida para prolongar a sessao sem nova senha, com usuario vinculado, prazo de expiracao, data de emissao e estado de revogacao.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das tentativas de acesso a recursos protegidos sem sessao valida sao recusadas antes da execucao da funcionalidade solicitada.
- **SC-002**: 100% das tentativas de renovacao com credencial expirada, inexistente ou revogada sao recusadas e exigem nova autenticacao.
- **SC-003**: 100% das operacoes cobertas por esta feature respeitam as permissoes definidas para ADMINISTRADOR, ATENDENTE, MECANICO e CLIENTE.
- **SC-004**: Usuarios autenticados conseguem consultar sua propria identidade e perfis ativos em uma unica requisicao durante a sessao.
- **SC-005**: O encerramento de sessao impede novas renovacoes derivadas da credencial invalidada em 100% dos casos testados.

## Assumptions

- As funcionalidades de cadastro, consulta e atualizacao citadas para cada perfil ja existem ou serao tratadas em features proprias; esta feature define apenas o acesso a elas.
- O sistema ja possui ou passara a possuir contas de usuario para perfis internos e para clientes que precisem acessar informacoes proprias.
- O rastreamento publico de ordem de servico permanecera acessivel sem autenticacao, desde que continue classificado como recurso publico pelo negocio.
- Contas administrativas terao manutencao de ciclo de vida fora do escopo desta feature, exceto pelo uso delas no processo de autenticacao e autorizacao.
