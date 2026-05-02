# Quickstart: Controle de Acesso Autenticado

## Objetivo

Validar localmente o fluxo completo de autenticacao, renovacao, logout e protecao por perfil apos a implementacao da feature.

## Pre-requisitos

1. Subir o banco PostgreSQL com `docker compose up -d`.
2. Garantir que as migrations novas de autenticacao foram aplicadas.
3. Garantir a existencia de ao menos um usuario ativo de teste, por exemplo:
   - `admin` com perfil `ADMINISTRADOR`
   - `atendente1` com perfil `ATENDENTE`
   - `mecanico1` com perfil `MECANICO`
   - `cliente1` com perfil `CLIENTE` e vinculado 1:1 a um registro de `Cliente`
4. Iniciar a aplicacao com `mvn spring-boot:run`.

## Fluxo 1: Login bem-sucedido por username ou email

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "senha123"
  }'
```

Tambem deve funcionar enviando o email no campo `username`, por exemplo `admin@teste.com`.

**Resultado esperado**:

- Retorno `200 OK`
- `accessToken` preenchido
- `refreshToken` preenchido
- `expiresIn` com valor positivo
- `accessToken` com claims `sub`, `username`, `roles`, `iat` e `exp`

## Fluxo 2: Consultar usuario autenticado

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

**Resultado esperado**:

- Retorno `200 OK`
- Payload com `id`, `username` e `roles`

## Fluxo 3: Renovar sessao

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN>"
  }'
```

**Resultado esperado**:

- Retorno `200 OK`
- Novo `accessToken`
- Novo `refreshToken`
- Mesmo usuario/perfis da sessao original
- O refresh token anterior deixa de ser aceito

## Fluxo 4: Logout por sessao

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN>"
  }'
```

**Resultado esperado**:

- Retorno `204 No Content`
- Apenas o refresh token informado fica inutilizavel
- Outras sessoes ativas do mesmo usuario continuam validas

## Fluxo 5: Confirmar refresh revogado

Repetir a chamada de refresh com o mesmo token revogado.

**Resultado esperado**:

- Retorno `401 Unauthorized`

## Fluxo 6: Validar multiplas sessoes

1. Efetuar dois logins consecutivos com o mesmo usuario.
2. Executar logout apenas com o `refreshToken` da primeira sessao.
3. Tentar renovar as duas sessoes.

**Resultado esperado**:

- A primeira sessao retorna `401 Unauthorized` no refresh
- A segunda sessao continua retornando `200 OK`

## Fluxo 7: Validar 401 em rota protegida

```bash
curl http://localhost:8080/api/v1/clientes
```

**Resultado esperado**:

- Retorno `401 Unauthorized`

## Fluxo 8: Validar 403 por perfil

1. Autenticar com um usuario de perfil `CLIENTE`.
2. Chamar uma rota restrita a administracao, por exemplo cadastro administrativo futuro ou um endpoint marcado para `ADMINISTRADOR`.

**Resultado esperado**:

- Retorno `403 Forbidden`

## Fluxo 9: Validar rota publica

```bash
curl http://localhost:8080/api/public/ordem-servico/rastreamento?codigo=OS-123
```

**Resultado esperado**:

- Rota continua acessivel sem token
- A implementacao de rastreamento em si permanece fora do escopo caso ainda nao exista
