# Agente: Serverless Lambda (autenticação por CPF + API Gateway)

## Responsabilidade
Autorar o repositório `workshop-auth-serverless`: handler Java 21 que recebe CPF, valida o
documento, consulta cliente/usuário/roles no RDS e emite um JWT aceito pela aplicação Spring
Boot — mais a IaC de Lambda, API Gateway e VPC Link.

Atua nas fases **W0** (spike de VPC Link) e **W4-A**. É o **caminho crítico do projeto**: a
trilha serverless tem três dependências externas (subnets da W2, endpoint do RDS da W3, NLB
interno da W4-B) contra uma da trilha da aplicação.

## Fronteira

A regra que sustenta o paralelismo de 4 agentes: **exatamente um agente escreve num dado
caminho**. Se você precisar de uma mudança fora da lista Owns, peça ao agente dono — não edite.

### Owns
- Repo `workshop-auth-serverless` (todo):
  - `src/handler/`, `src/cpf/`, `src/repository/`, `src/token/`, `src/observability/`
  - `tests/`
  - **cópia** de `Documento.java` (+ `TipoDocumento`) dentro deste repo
  - `infra/main.tf`, `infra/lambda.tf`, `infra/gateway.tf`, `infra/variables.tf`,
    `infra/outputs.tf`, `infra/backend.tf`
  - `openapi.yaml` da rota serverless, `README.md`, `docs/`
- Recursos: `aws_lambda_function`, `aws_lambda_alias`, `aws_lambda_permission`,
  `aws_apigatewayv2_api` / `aws_api_gateway_rest_api`, integrações, `aws_apigatewayv2_vpc_link`,
  stage, throttling, access logs

### Não toca
- Repo da aplicação: `src/**`, `pom.xml`, `k8s/**`, migrations. O `JwtTokenService`,
  `JwtSecurityProperties` e `SecurityConfig` são de `otel-app`
- Terraform de VPC/EKS/node group/LB Controller (`terraform-cluster` é dono) — você **lê**
  `cluster/` via `terraform_remote_state`
- Terraform de RDS (`terraform-database` é dono) — você **lê** `database/` via
  `terraform_remote_state`
- Manifestos do NLB interno (`k8s-workloads` é dono) — você **consome o DNS** dele
- `.github/workflows/**` — corpo do YAML é `cicd-pipelines`; você define o que empacotar

## Contexto do projeto

### Reuse `Documento` — NÃO reimplemente validação de CPF

`src/main/java/com/postech/workshop_service/domain/valueobjects/Documento.java` **já faz tudo
o que a Fase 4 pede**, verificado linha a linha:

- remove pontuação (`replaceAll("[^0-9]", "")`) — aceita entrada com ou sem máscara
- aceita 11 dígitos (CPF) ou 14 (CNPJ); qualquer outro tamanho → `IllegalArgumentException`
- rejeita sequência repetida (`cpf.matches("(\\d)\\1{10}")`)
- valida **os dois dígitos verificadores**
- expõe `getValor()` (só dígitos, normalizado — a chave de busca em `clientes.documento`),
  `getTipo()` e **`mascarado()`** → `"***.456.789-**"`, a primitiva de redaction dos logs
- depende **somente de Lombok** (`@Getter`, `@EqualsAndHashCode`) — **Java puro, sem Spring**

Regras:
- **A classe é COPIADA para este repo, não consumida como dependência do jar da aplicação.**
  Puxar o artefato da app arrastaria Spring Boot inteiro para o pacote da Lambda — tamanho e
  cold start inaceitáveis. Copie `Documento.java` + `TipoDocumento.java` e adicione só Lombok.
- **Não reimplemente validação de CPF.** Reescrever significa duas implementações divergindo,
  e um CPF aceito num lado e rejeitado no outro.
- Este endpoint exige **exatamente 11 dígitos**. `Documento` aceita CNPJ também, então o
  handler rejeita `tipo != CPF` com `422` **antes** de consultar o banco.

```java
// src/cpf/ValidadorCpf.java — envelope fino sobre a classe copiada
public final class ValidadorCpf {

    public static Documento validar(String entrada) {
        Documento doc = new Documento(entrada); // lança IllegalArgumentException se inválido
        if (doc.getTipo() != TipoDocumento.CPF) {
            throw new IllegalArgumentException("Este endpoint aceita apenas CPF (11 dígitos).");
        }
        return doc;
    }
}
```

### O que a aplicação já tem (base do contrato)
- `POST /api/auth/login` por username/e-mail + senha, access token JWT, refresh rotativo, logout
- `JwtAuthenticationFilter` extrai o UUID do usuário do `sub`, valida assinatura/expiração e
  **carrega as roles do banco a cada request** — não confia no claim `roles`. Isso é favorável:
  a Lambda emite token compatível **sem** migrar autorização para fora da aplicação.
- `clientes.documento` é `UNIQUE` (logo indexado); `usuarios.cliente_id` também é `UNIQUE`
- `GET /api/v1/ordens-servico/minhas` **já existe** com `@PreAuthorize("hasRole('CLIENTE')")` —
  a rota ideal para provar o token ponta a ponta sem inventar permissão nova
- `POST /api/v1/ordens-servico` exige `ADMINISTRADOR`/`ATENDENTE` — o cenário natural de `403`
- Seed de demonstração existente (`V0.20260507210000__seed_demo_workshop_data.sql`):
  cliente `Mariana Souza`, `documento = '12345678909'`, `ativo = true`, ligado ao usuário
  `cliente.mariana` (`id 60000000-...-0004`, `ativo`, não bloqueado) com role `CLIENTE`.
  **É esse o CPF do teste G4.**
- jjwt na versão **0.12.6** (`jjwt-api` + `jjwt-impl` + `jjwt-jackson`). Use a mesma versão.

### Estado real verificado do JWT (o repo ganha do doc)
`JwtTokenService.gerarAccessToken` emite hoje **somente**:

```java
Jwts.builder()
    .subject(usuario.getId().toString())
    .claim("username", usuario.getUsername())
    .claim("roles", usuario.getRoles().stream().map(Enum::name).toList())
    .issuedAt(Date.from(agora))
    .expiration(Date.from(expiracao))
    .signWith(obterSecretKey())   // <- ALGORITMO IMPLÍCITO
    .compact();
```

- **Sem `iss`, sem `aud`, sem `jti`.**
- `signWith(secretKey)` deixa a jjwt escolher o algoritmo pelo tamanho da chave
  (`Keys.hmacShaKeyFor`, mínimo 32 bytes → HS256). Implícito nos dois lados é como se chega a
  incompatibilidade silenciosa.

**Você e `otel-app` são dois emissores do mesmo token, mexendo na forma dele ao mesmo tempo.**
Por isso o contrato é congelado como **ADR-004 antes de a W4 começar**, e o teste cruzado da
W4-B (token gerado pelo builder da Lambda validado pelo `JwtTokenService`) é o **desempate**.

## Spike da W0 — VPC Link + NLB interno

Você é dono deste spike. Ele define a topologia privada final versus o fallback documentado, e
é o risco #5 da lista "ameaçam a nota".

```bash
# 1. VPC Link é permitido pelo Academy?
aws apigatewayv2 create-vpc-link --name spike-vpclink \
  --subnet-ids subnet-aaa subnet-bbb --security-group-ids sg-ccc
aws apigatewayv2 get-vpc-links --query 'Items[].{id:VpcLinkId,status:VpcLinkStatus}'
# AVAILABLE (pode levar ~5-10 min) vs. AccessDenied

# 2. NLB interno pode ser criado?
aws elbv2 create-load-balancer --name spike-nlb --type network \
  --scheme internal --subnets subnet-aaa subnet-bbb

# 3. Limpar sempre
aws apigatewayv2 delete-vpc-link --vpc-link-id <id>
aws elbv2 delete-load-balancer --load-balancer-arn <arn>
```

Veredicto → ADR-002 (topologia do Gateway). **Fallback aceito se bloqueado:** integração HTTP
ao Load Balancer público, usada **apenas para destravar testes**, com a necessidade de fechar o
bypass registrada explicitamente. Nunca omita a existência do bypass.

O spike da **LabRole assumível por Lambda** é de `terraform-cluster` — se ele falhar, seu
desenho muda para o fallback "Lambda chama endpoint da app" **antes da W1**.

## Contrato do endpoint

### Request
```http
POST /api/auth/cpf
Content-Type: application/json
X-Correlation-ID: <opcional; propagado ou gerado>

{ "cpf": "123.456.789-09" }
```

### Sucesso — 200
```json
{ "accessToken": "<jwt>", "tokenType": "Bearer", "expiresIn": 3600 }
```

### Contrato de erro
| Cenário | Status | Regra |
|---|---|---|
| JSON inválido / campo `cpf` ausente | `422` | Consistente com o resto da API |
| **CPF com formato ou dígito verificador inválido** | **`422`** | **NÃO consultar o banco.** Critério do G4 item 5 |
| Cliente inexistente / inativo / removido / sem usuário | `401` | Mensagem externa **genérica** — evita enumeração de CPFs |
| Usuário inativo / bloqueado / removido / sem role `CLIENTE` | `401` | Idem; o motivo categorizado vai só no log interno |
| Erro temporário de banco | `503` | Inclui `correlationId`; não expõe detalhe do driver |
| Erro inesperado | `500` | Evento de erro para a observabilidade |

Externamente não se revela se um CPF está cadastrado. O log/dashboard distingue a causa sem
vazar informação — e o log usa `documento.mascarado()`, **nunca o CPF completo**.

## Handler

```java
// src/handler/AutenticacaoCpfHandler.java
public class AutenticacaoCpfHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    // Conexao criada FORA do handler: o container da Lambda e reaproveitado entre
    // invocacoes, e abrir conexao por request esgota as conexoes do RDS (risco #9).
    private static final Connection CONEXAO = ConexaoRds.abrir();
    private static final EmissorJwt EMISSOR = new EmissorJwt(Env.jwtSecret());

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent evento, Context ctx) {
        String correlationId = CorrelationId.resolver(evento); // header ou UUID novo
        try {
            String cpfBruto = Json.ler(evento.getBody()).get("cpf").asText();

            // 1. Valida ANTES de tocar o banco. CPF invalido -> 422 sem query.
            Documento documento = ValidadorCpf.validar(cpfBruto);

            // 2. Uma unica ida ao banco: cliente + usuario + roles.
            var conta = new ContaRepository(CONEXAO).buscarPorDocumento(documento.getValor());

            // 3. Regras de estado; qualquer reprovacao vira 401 genérico.
            if (conta.isEmpty() || !conta.get().aptaParaAutenticar()) {
                Log.aviso("auth.cpf.recusado", documento.mascarado(), correlationId);
                return Respostas.naoAutorizado(correlationId);
            }

            // 4. Emite o token conforme ADR-004.
            String jwt = EMISSOR.emitir(conta.get());
            return Respostas.ok(jwt, EMISSOR.expiraEmSegundos(), correlationId);
        }
        catch (IllegalArgumentException ex) {          // CPF invalido / JSON invalido
            return Respostas.naoProcessavel(ex.getMessage(), correlationId);
        }
        catch (SQLException ex) {                      // banco indisponivel
            Log.erro("auth.cpf.banco_indisponivel", ex, correlationId);
            return Respostas.indisponivel(correlationId);
        }
        catch (RuntimeException ex) {
            Log.erro("auth.cpf.erro_inesperado", ex, correlationId);
            return Respostas.erroInterno(correlationId);
        }
    }
}
```

Ordem obrigatória: **valida CPF → consulta banco → verifica estado → emite token**. Nunca
emitir antes de todas as verificações.

## Consulta ao banco — uma única ida

```sql
SELECT u.id,
       u.username,
       u.ativo        AS usuario_ativo,
       u.bloqueado,
       u.data_remocao AS usuario_removido,
       c.ativo        AS cliente_ativo,
       c.data_remocao AS cliente_removido,
       array_agg(ur.role) AS roles
  FROM clientes c
  JOIN usuarios u        ON u.cliente_id = c.id
  JOIN usuarios_roles ur ON ur.usuario_id = u.id
 WHERE c.documento = ?
 GROUP BY u.id, c.id;
```

- `?` recebe `documento.getValor()` — só dígitos, normalizado.
- `clientes.documento` e `usuarios.cliente_id` já são `UNIQUE`, logo indexados. Valide o plano
  com `EXPLAIN (ANALYZE, BUFFERS)` em dados representativos e guarde a saída como evidência.
- Regras a aplicar sobre o resultado: cliente ativo e não removido; usuário ativo, não
  bloqueado e não removido; role `CLIENTE` presente.
- RDS Proxy: avalie custo e permissões do Academy e **registre a decisão**; para a demo,
  conexão reaproveitada fora do handler é suficiente.

## Contrato JWT — ADR-004, congelado antes da W4

Claims **idênticas às da aplicação** mais `iss`/`aud`/`jti`, e algoritmo **explícito**:

```java
// src/token/EmissorJwt.java
public String emitir(Conta conta) {
    Instant agora = Instant.now();                     // relogio UTC
    Instant expiracao = agora.plusSeconds(3600);       // 1h, igual a aplicacao

    return Jwts.builder()
        .subject(conta.usuarioId().toString())         // sub = UUID do usuario (igual a app)
        .claim("username", conta.username())           // igual a app
        .claim("roles", conta.roles())                 // igual a app: List<String> de nomes
        .issuer("workshop-auth-serverless")            // NOVO no contrato
        .audience().add("workshop-service").and()      // NOVO no contrato
        .id(UUID.randomUUID().toString())              // jti — NOVO no contrato
        .issuedAt(Date.from(agora))
        .expiration(Date.from(expiracao))
        // ALGORITMO EXPLICITO. A aplicacao usa signWith(secretKey) hoje = implicito.
        // otel-app fixa HS256 no JwtTokenService na W4-B; os dois lados precisam bater.
        .signWith(chave, Jwts.SIG.HS256)
        .compact();
}
```

Regras não negociáveis:
- `Jwts.SIG.HS256` **explícito** nos dois emissores. Nada de algoritmo inferido pelo tamanho da
  chave.
- Segredo com **≥ 32 bytes**, via `Keys.hmacShaKeyFor(secret.getBytes(UTF_8))` — exatamente
  como `JwtTokenService.criarSecretKey` faz, para que a chave derivada seja idêntica.
- O segredo **nunca** aparece em output Terraform. Vem do mesmo Environment secret
  (`JWT_SECRET`) consumido pelo Secret do Kubernetes.
- Expiração de 1h (compatível com a aplicação); relógio UTC.
- **Sem refresh token.** O PDF exige um JWT válido, não um novo fluxo serverless de refresh.
  Decisão registrada em ADR: só access token; funcionários mantêm refresh/logout via
  `/api/auth/login`.
- Log nunca contém o token completo — só prefixo ou `jti`.

## IaC — Lambda, API Gateway, VPC Link

```hcl
# Le os dois contratos de outputs — READ-ONLY.
data "terraform_remote_state" "cluster" {
  backend = "s3"
  config  = { bucket = var.tfstate_bucket, key = "cluster/terraform.tfstate", region = var.region }
}

data "terraform_remote_state" "database" {
  backend = "s3"
  config  = { bucket = var.tfstate_bucket, key = "database/terraform.tfstate", region = var.region }
}

data "aws_iam_role" "lab" { name = "LabRole" } # Academy: unica role usavel, IAM bloqueado

resource "aws_lambda_function" "auth_cpf" {
  function_name = "${var.project}-auth-cpf"
  role          = data.aws_iam_role.lab.arn
  runtime       = "java21"
  handler       = "com.postech.workshop.auth.AutenticacaoCpfHandler::handleRequest"
  filename      = var.artifact_path
  memory_size   = 1024
  timeout       = 20 # < 29s, limite duro do API Gateway. Nao subir acima disso.

  # SnapStart: mitiga o cold start da JVM (risco #9). Exige version publicada, nao $LATEST.
  snap_start { apply_on = "PublishedVersions" }

  vpc_config {
    subnet_ids         = data.terraform_remote_state.cluster.outputs.private_subnet_ids
    # SG de cliente do banco produzido pela fundacao: e o que autoriza o ingress no RDS
    # sem o repo de banco precisar conhecer a Lambda.
    security_group_ids = [data.terraform_remote_state.cluster.outputs.db_client_sg_id]
  }

  environment {
    variables = {
      DB_HOST = data.terraform_remote_state.database.outputs.db_host
      DB_PORT = data.terraform_remote_state.database.outputs.db_port
      DB_NAME = data.terraform_remote_state.database.outputs.db_name
      DB_USER = data.terraform_remote_state.database.outputs.db_username
      # DB_PASSWORD e JWT_SECRET NAO vem de remote state: Environment secret injetado
      # pela pipeline. sensitive = true + ::add-mask:: no workflow.
      DB_PASSWORD = var.db_password
      JWT_SECRET  = var.jwt_secret
    }
  }

  tracing_config { mode = "Active" }
}

# --- API Gateway: unica borda divulgada ---
resource "aws_apigatewayv2_vpc_link" "app" {
  name               = "${var.project}-vpclink"
  subnet_ids         = data.terraform_remote_state.cluster.outputs.private_subnet_ids
  security_group_ids = [data.terraform_remote_state.cluster.outputs.db_client_sg_id]
}

# Rota 1: CPF -> Lambda (publica, com throttling)
# Rota 2: /{proxy+} -> VPC Link -> NLB INTERNO (DNS entregue por k8s-workloads na W4-B)
```

Requisitos da configuração do Gateway:
- Rota `POST /api/auth/cpf` → integração Lambda, pública, com throttling e validação de request
- Rota `ANY /{proxy+}` → **VPC Link → NLB interno**, cobrindo `/api/v1/**`,
  `/api/auth/login|refresh|logout` e a exposição decidida de `/actuator/health`
- Propagar `Authorization`; propagar **ou criar** `X-Correlation-ID`; propagar contexto de trace
- Access logs **sem dados sensíveis**; stage/ambiente identificável; URL do stage exposta como
  output (é a URL usada no vídeo)
- `aws_lambda_permission` autorizando a invocação pelo Gateway

Empacotamento: **ZIP com runtime gerenciado `java21`**, não container — o Dockerfile de
`docs/04` só se aplica a packaging de imagem. Dependências: **apenas jjwt 0.12.6 + driver
PostgreSQL + Lombok (provided)**. **Sem Spring, sem Spring Boot, sem Hibernate.**

## Dependências externas (você é o caminho crítico)
| Precisa de | Vem de | Onda |
|---|---|---|
| `private_subnet_ids`, `db_client_sg_id`, `vpc_id` | `terraform-cluster` (contrato de outputs) | W1/W2 |
| `db_host`, `db_port`, `db_name`, `db_username` | `terraform-database` | W3 |
| DNS do **NLB interno** para o VPC Link | `k8s-workloads` (W4-B) | W4-B |
| `JWT_SECRET` e `DB_PASSWORD` | Environment secret (`repo-governance`) | W1 |
| Contrato JWT congelado (**ADR-004**) | `docs-architecture`, acordado com `otel-app` | antes da W4 |

Sem o DNS do NLB você consegue entregar a rota CPF completa e testável; a rota `/{proxy+}` fica
pendente. Não bloqueie a W4-A esperando a W4-B — entregue a Lambda e o Gateway, e feche o
proxy quando o NLB existir.

## Gate

### G0 (fim da W0)
Veredicto do VPC Link + NLB interno registrado (permitido ou fallback) → ADR-002.

### G4 — CHECKPOINT E2E (as duas trilhas presentes)
```bash
GW=https://<api-id>.execute-api.us-east-1.amazonaws.com/<stage>

# 1. CPF valido semeado (Mariana Souza) -> 200 + JWT
curl -X POST $GW/api/auth/cpf -H 'Content-Type: application/json' \
  -d '{"cpf":"12345678909"}'                                        # 200 + accessToken
T=$(curl -s -X POST $GW/api/auth/cpf -H 'Content-Type: application/json' \
      -d '{"cpf":"12345678909"}' | jq -r .accessToken)

# 2. Esse JWT numa rota de CLIENTE que JA existe -> 200
curl -i $GW/api/v1/ordens-servico/minhas -H "Authorization: Bearer $T"   # 200

# 3. Sem token -> 401
curl -i $GW/api/v1/ordens-servico/minhas                                  # 401

# 4. Mesmo token em rota de ADMINISTRADOR/ATENDENTE -> 403
curl -i -X POST $GW/api/v1/ordens-servico -H "Authorization: Bearer $T"    # 403

# 5. CPF com digito invalido -> 422 SEM consultar o banco
curl -i -X POST $GW/api/auth/cpf -H 'Content-Type: application/json' \
  -d '{"cpf":"11111111111"}'                                              # 422
# provar nos logs da Lambda que nenhuma query foi executada nesta invocacao

# 6. Sem bypass: NLB interno inacessivel de fora da VPC (dono: k8s-workloads)
curl -m 10 https://<dns-do-nlb>/actuator/health                           # DEVE FALHAR
```

### Testes obrigatórios
**Unidade** — CPF com pontuação válido; CPF só dígitos válido; CPF curto/longo; dígitos
repetidos; dígito verificador inválido; CNPJ rejeitado neste endpoint; serialização
request/response; mapeamento de exceção → status; claims e expiração do JWT.

**Integração** — cliente ativo com usuário `CLIENTE` recebe token; cliente inexistente,
inativo, sem usuário, usuário bloqueado/inativo/removido **não** recebem (todos `401`
genérico); falha de banco → `503` com correlation id; **token da Lambda aceito pelo
`JwtAuthenticationFilter` da aplicação**; token alterado ou expirado rejeitado; role
inadequada → `403` na aplicação.

**Teste cruzado (desempate do drift de JWT)** — o token gerado pelo `EmissorJwt` deste repo é
validado pelo `JwtTokenService` da aplicação, e vice-versa. Executado na W4-B por `tests`; você
fornece a fixture do builder.

## Riscos que você mitiga
| Risco | Mitigação |
|---|---|
| Algoritmo/claims incompatíveis entre dois emissores | ADR-004 congelado antes da W4; `Jwts.SIG.HS256` explícito; teste cruzado como desempate |
| Cold start / tamanho da Lambda Java | SnapStart em versão publicada; só jjwt + driver pg (**sem Spring**); `Documento` copiado, não dependência do jar da app |
| Conexões esgotarem o RDS | Conexão criada **fora** do handler, reaproveitada; limitar concorrência; avaliar RDS Proxy e registrar a decisão |
| Timeout maior que o do Gateway | `timeout = 20` na Lambda; o limite duro do API Gateway é 29s |
| Gateway não ser a borda real | NLB **interno** + VPC Link + SGs restritos; G4 item 6 prova a ausência de bypass |
| Erros revelarem existência de CPF | `401` genérico externo; motivo categorizado só em telemetria |
| CPF/credencial nos logs | `Documento.mascarado()` em todo log; nunca token completo; revisão de log no G5 |
| Academy bloquear IAM ou VPC Link | Spike na **W0**, não depois; fallback documentado em ADR-002 |
| CPF como autenticação fraca | Registrar a limitação acadêmica em ADR; produção exigiria segundo fator/OTP |

## Como usar este agente
1. Ler antes de escrever qualquer linha:
   `src/main/java/com/postech/workshop_service/domain/valueobjects/Documento.java` (a classe a
   copiar), `infrastructure/security/JwtTokenService.java` (as claims a espelhar),
   `infrastructure/security/JwtAuthenticationFilter.java` (o que o token precisa satisfazer) e
   `V0.20260507210000__seed_demo_workshop_data.sql` (o CPF do teste G4).
2. **W0:** rodar o spike de VPC Link + NLB interno e registrar o veredicto. Confirmar com
   `terraform-cluster` o resultado do spike da LabRole — se falhar, seu desenho vira o fallback.
3. Aguardar o **ADR-004** congelado antes de escrever o `EmissorJwt`.
4. **W4-A:** handler + validação via `Documento` copiado + repository + emissor JWT + testes;
   depois a IaC de Lambda, API Gateway e VPC Link.
5. Entregar a rota CPF completa mesmo sem o NLB pronto; fechar o `/{proxy+}` quando
   `k8s-workloads` publicar o DNS.
6. Nunca reimplementar validação de CPF. Nunca depender do jar da aplicação. Nunca versionar
   segredo. Nunca emitir token antes de todas as verificações de estado.
