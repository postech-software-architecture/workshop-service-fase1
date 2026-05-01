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
   - `cliente1` com perfil `CLIENTE`
4. Iniciar a aplicacao com `mvn spring-boot:run`.

## Fluxo 1: Login bem-sucedido

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "senha123"
  }'
```

**Resultado esperado**:

- Retorno `200 OK`
- `accessToken` preenchido
- `refreshToken` preenchido
- `expiresIn` com valor positivo

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
- Mesmo usuario/perfis da sessao original

## Fluxo 4: Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN>"
  }'
```

**Resultado esperado**:

- Retorno `204 No Content` ou `200 OK`, conforme contrato final implementado
- O refresh token informado fica inutilizavel

## Fluxo 5: Confirmar refresh revogado

Repetir a chamada de refresh com o mesmo token revogado.

**Resultado esperado**:

- Retorno `401 Unauthorized`

## Fluxo 6: Validar 401 em rota protegida

```bash
curl http://localhost:8080/api/v1/clientes
```

**Resultado esperado**:

- Retorno `401 Unauthorized`

## Fluxo 7: Validar 403 por perfil

1. Autenticar com um usuario de perfil `CLIENTE`.
2. Chamar uma rota restrita a administracao, por exemplo cadastro administrativo futuro ou um endpoint marcado para `ADMINISTRADOR`.

**Resultado esperado**:

- Retorno `403 Forbidden`

## Fluxo 8: Validar rota publica

```bash
curl http://localhost:8080/api/public/ordem-servico/rastreamento?codigo=OS-123
```

**Resultado esperado**:

- Rota continua acessivel sem token
- A implementacao de rastreamento em si permanece fora do escopo caso ainda nao exista
