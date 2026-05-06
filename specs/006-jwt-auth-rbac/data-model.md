# Data Model: Controle de Acesso Autenticado

## Entidade: Usuario

**Descricao**: Representa uma conta autenticavel do sistema, usada por perfis internos e por clientes com acesso autenticado.

### Campos

| Campo | Tipo | Regras |
|-------|------|--------|
| id | UUID | Obrigatorio, chave primaria |
| username | texto | Obrigatorio, unico, nao vazio |
| email | texto | Opcional para autenticacao, unico quando informado |
| senhaHash | texto | Obrigatorio, armazenado em formato nao reversivel |
| ativo | booleano | Obrigatorio, define se a conta pode autenticar |
| bloqueado | booleano | Obrigatorio, impede autenticacao quando verdadeiro |
| dataCriacao | data/hora | Obrigatorio |
| dataUltimaAtualizacao | data/hora | Obrigatorio |
| dataRemocao | data/hora | Opcional |

### Relacionamentos

- Um `Usuario` possui um ou mais `PerfilAcesso`.
- Um `Usuario` possui zero ou muitos `RefreshToken`, um por sessao autenticada.
- Um `Usuario` com perfil `CLIENTE` deve estar associado a exatamente um `Cliente`.
- Um `Usuario` sem perfil `CLIENTE` nao pode possuir associacao com `Cliente`.

### Regras de Validacao

- `username` deve ser unico no sistema.
- O login aceita `username` ou `email`, ambos unicos quando informados.
- `senhaHash` nunca pode ser persistido em texto puro.
- Usuario inativo, bloqueado ou removido logicamente nao pode autenticar.
- Usuario autenticado deve expor pelo menos um perfil de acesso valido.
- Contas com perfil `CLIENTE` devem respeitar o vinculo 1:1 com o agregado `Cliente`.

### Comportamentos

- Ativar/desativar conta.
- Bloquear/desbloquear conta.
- Alterar senha com substituicao do hash.
- Informar authorities derivadas dos perfis.

## Entidade: PerfilAcesso

**Descricao**: Valor de dominio que representa a responsabilidade autorizativa do usuario.

### Valores Permitidos

- `ADMINISTRADOR`
- `ATENDENTE`
- `MECANICO`
- `CLIENTE`

### Regras

- O conjunto de perfis nao pode estar vazio para contas ativas.
- Perfis sao a unica fonte de verdade para conversao em authorities do Spring Security.
- O perfil `CLIENTE` habilita somente contas vinculadas ao proprio registro de `Cliente`.

## Entidade: RefreshToken

**Descricao**: Representa a credencial persistida de renovacao de sessao emitida apos login bem-sucedido.

### Campos

| Campo | Tipo | Regras |
|-------|------|--------|
| id | UUID | Obrigatorio, chave primaria |
| token | texto | Obrigatorio, unico, nao vazio |
| usuarioId | UUID | Obrigatorio, referencia a `Usuario` |
| dataExpiracao | data/hora | Obrigatorio |
| revogado | booleano | Obrigatorio |
| dataCriacao | data/hora | Obrigatorio |
| dataUltimaAtualizacao | data/hora | Obrigatorio |
| dataRevogacao | data/hora | Opcional |

### Relacionamentos

- Cada `RefreshToken` pertence a exatamente um `Usuario`.

### Regras de Validacao

- Token revogado nao pode ser reutilizado para renovacao.
- Token expirado nao pode gerar novo access token.
- Token deve ser unico para evitar colisao entre sessoes.
- Cada renovacao bem-sucedida gera um novo refresh token e revoga o anterior.

### Estados

| Estado | Descricao |
|--------|-----------|
| Ativo | Pode renovar sessao se nao estiver expirado |
| Expirado | Nao pode mais ser usado |
| Revogado | Nao pode mais ser usado, mesmo antes da expiracao |

### Transicoes

- `Ativo -> Revogado`: logout ou invalidacao de seguranca.
- `Ativo -> Revogado`: refresh token rotacionado por renovacao bem-sucedida.
- `Ativo -> Expirado`: passagem do tempo alem da data limite.

## Visao Derivada: SessaoAutenticada

**Descricao**: Projecao retornada ao cliente autenticado e usada internamente durante a request.

### Campos

| Campo | Tipo | Origem |
|-------|------|--------|
| usuarioId | UUID | `Usuario.id` |
| username | texto | `Usuario.username` |
| clienteId | UUID | `Usuario.clienteId` quando aplicavel |
| perfis | lista | `PerfilAcesso` do usuario |
| expiraEm | numero | calculado a partir do access token emitido |

### Uso

- Resposta do endpoint `me`.
- Montagem do principal autenticado no contexto de seguranca.

## Tabelas Previstas

- `usuarios`
- `usuarios_roles`
- `refresh_tokens`

## Regras de Persistencia

- Todas as tabelas novas devem possuir PK UUID.
- Tabelas de entidade devem possuir `data_criacao` e `data_ultima_atualizacao`.
- Colunas e tabelas devem ter comentarios SQL no Flyway.
- FKs devem seguir o padrao `fk_origem_destino`.
