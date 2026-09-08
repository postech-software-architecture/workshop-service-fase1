# Agente: Repo Governance (gh CLI)

## Responsabilidade
Criar e governar os repositórios da Fase 3 via `gh` CLI: criação dos 3 repos novos + `soat-architecture`,
branch protection, required checks, GitHub Environments `homolog`/`prod`, secrets por ambiente e secret
scanning. Também produz as **evidências** de governança exigidas pelos gates G1 e G6.

Este agente configura **settings de repositório**. Não escreve código nem conteúdo de workflow.

## Contexto do projeto

### Repositórios da Fase 3

| Repo | Responsabilidade | Origem |
|---|---|---|
| `workshop-service` | App Spring Boot + workloads Kubernetes + docs centrais | Repo atual (já existe, **público**) |
| `workshop-infra-kubernetes` | VPC, subnets, EKS, node group, metrics-server, AWS LB Controller | Extração de `infra/eks` |
| `workshop-infra-database` | RDS PostgreSQL, subnet group, SG do banco | Extração dos blocos RDS de `infra/eks/main.tf` |
| `workshop-auth-serverless` | Lambda de auth por CPF, API Gateway, VPC Link | Novo |

Mais `soat-architecture` como colaborador em qualquer cenário privado (exigência do doc 07).

### Contexto organizacional verificado

| Fato | Valor | Consequência |
|---|---|---|
| Papel do usuário na org `postech-software-architecture` | **admin** | Pode criar repos e configurar branch protection |
| `members_can_create_repos` | `true` | Criação via `gh` é viável |
| Plano da org | **free** | Branch protection **não funciona em repo privado** |
| Repo atual `workshop-service` | **público** | Já tem branch protection viável |

**Decisão derivada, e é a razão de ser desta onda:** criar os 3 repos novos como **PÚBLICOS**
(`gh repo create --public`). No plano free, público é a única forma de ter branch protection real, e
isso **elimina a ressalva de "limitação do plano"** que o doc 07 previa como fallback ("manter processo
de PR sem push direto, documentar a limitação"). Não há fallback a documentar: há proteção real.

O preço dessa decisão é que **todo o conteúdo fica exposto**. Daí o sub-gate abaixo, que é bloqueante.

## Fronteira

### Owns
- `gh repo create` dos 3 repos novos + `soat-architecture`
- Branch protection / rulesets nos 4 repos
- Required status checks (os **nomes** dos checks; o conteúdo dos jobs é do `cicd-pipelines`)
- GitHub Environments `homolog` e `prod`, com reviewers de aprovação
- Secrets de repo e de Environment
- Secret scanning / push protection
- Tag `phase3-baseline` no repo da app
- Evidências de governança (capturas e JSON de `gh api`)

### Não toca
- Nenhum arquivo `.tf` — é do `terraform-cluster` / `terraform-database`
- Nada em `src/**` — é do `otel-app` / `tests`
- **Corpo de workflow** (`.github/workflows/**`) — é do `cicd-pipelines`. Este agente define
  que o check `Build, test (unit + integration) and coverage` é obrigatório; quem escreve
  esse job é o `cicd-pipelines`
- `k8s/**` — é do `k8s-workloads`
- `docs/architecture/**` — é do `docs-architecture`

## Sub-gate de segurança (BLOQUEANTE — antes de qualquer publicação)

Os repos ficam públicos nesta onda. Nenhum `gh repo create` roda antes de os 4 itens abaixo
estarem verdes.

### 1. Segredo JWT default commitado — verificado no repo

`src/main/resources/application.yml`, **linha 31**:

```yaml
seguranca:
  jwt:
    secret: ${JWT_SECRET:d3f8a1c2b4e7f0a9d6c3b8e5f2a1d4c7b0e3f6a9d2c5b8e1f4a7d0c3b6e9f2a5}
```

64 caracteres hex como **valor default do placeholder**. Isso significa que a app sobe com
uma chave HS256 conhecida publicamente se `JWT_SECRET` não estiver no ambiente.

Está no histórico git — confirmado:

```bash
git log --oneline -S "d3f8a1c2b4e7f0a9d6c3b8e5f2a1d4c7b0e3f6a9d2c5b8e1f4a7d0c3b6e9f2a5" \
  -- src/main/resources/application.yml
# 118e4b3 config: add default JWT_SECRET and enable Flyway out-of-order
```

Duas ações, ambas obrigatórias e **distintas**:

- **Remover o default** (fail-fast: `${JWT_SECRET}` sem valor após os dois-pontos). A execução
  dessa edição é do agente `otel-app` na W4-B — este agente **verifica e bloqueia**, não edita `src/`.
- **Rotacionar o segredo.** Remover do arquivo não remove do histórico. O valor `d3f8a1c2…f2a5`
  deve ser tratado como **comprometido permanentemente** e nunca reaparecer em nenhum
  Environment secret. Gere um novo:

```bash
openssl rand -hex 32   # 64 hex chars = 256 bits, compatível com Keys.hmacShaKeyFor
```

Reescrever o histórico (`git filter-repo`) **não** é recomendado aqui: o repo já é público, o valor
já circulou, e a reescrita quebra os PRs e as referências existentes que servem de evidência.
Rotação + `.env.example` sem valor real é a mitigação aceita. Registre isso na ADR de segurança.

### 2. `.gitignore` — divergência real encontrada

O doc 07 exige "nenhum `terraform.tfvars` sensível versionado". Estado verificado:

- `infra/.gitignore` linha 9 tem `*.tfvars` → cobre `infra/**`
- O **`.gitignore` da raiz NÃO tem `*.tfvars`** — cobre `.env` (linha 184) e `.env.local`, mas não tfvars

Consequência direta: os **3 repos novos**, que nascem da extração de `infra/`, não herdam essa regra
se o `.gitignore` da raiz for usado como base. Cada repo novo precisa nascer com, no mínimo:

```gitignore
# Terraform
*.tfvars
!*.tfvars.example
.terraform/
*.tfstate
*.tfstate.*
.terraform.lock.hcl   # avaliar: manter versionado é boa prática; nunca o state
crash.log
plan.out
plan.json

# Secrets
.env
.env.local
kubeconfig
*.pem
```

Verificação antes de publicar:

```bash
git ls-files | grep -iE 'tfvars$|\.env$|tfstate|kubeconfig' && echo "FALHA: arquivo sensível versionado"
git check-ignore -v terraform.tfvars .env      # deve casar em TODOS os 4 repos
```

Hoje só existem `terraform.tfvars.example` e `.env.example` versionados — corretos, e devem
permanecer **sem valor real** (regra do doc 07).

### 3. Varredura de segredos no histórico

```bash
# Instalar gitleaks se necessário; rodar contra o HISTÓRICO, não só a árvore
gitleaks detect --source . --log-opts="--all" --report-path /tmp/gitleaks-app.json
gitleaks detect --source . --no-git    # árvore de trabalho
```

Qualquer achado que **não** seja o segredo JWT já conhecido é bloqueante e vira uma decisão
explícita (rotacionar + documentar) antes de publicar.

### 4. Nenhum k8s Secret real versionado
Regra do doc 07. Confirmar que só existem `*.example`:

```bash
git ls-files | grep -i secret
```

## Padrão: criação dos repositórios

```bash
ORG=postech-software-architecture

for REPO in workshop-infra-kubernetes workshop-infra-database workshop-auth-serverless; do
  gh repo create "$ORG/$REPO" \
    --public \
    --description "Fase 3 — <responsabilidade em uma linha>" \
    --add-readme \
    --disable-wiki
done

# Repositório de documentação/arquitetura compartilhada (exigência do doc 07)
gh repo create "$ORG/soat-architecture" --public --add-readme --disable-wiki
```

`--public` não é preferência: é o que habilita branch protection no plano free. **Nunca** use
`--private` nestes repos sem antes reabrir a decisão com o time.

### Tag de baseline (antes da extração)

Roda **antes** de o `terraform-cluster` mover qualquer `.tf` para fora do repo da app. É o ponto
de retorno se a extração perder algo (risco "perder histórico ou alterações na migração" do doc 01).

```bash
git tag -a phase3-baseline -m "Baseline antes da extração de infra para os repos da Fase 3"
git push origin phase3-baseline
gh api "repos/$ORG/workshop-service/git/refs/tags/phase3-baseline" | jq '.object.sha'  # evidência
```

## Padrão: branch protection nos 4 repos

Uma aprovação, checks obrigatórios, sem force-push, sem deleção. Os nomes dos checks vêm do
`cicd-pipelines` — este agente só os registra como obrigatórios.

```bash
protect() {
  local REPO=$1 BRANCH=$2; shift 2
  local CONTEXTS=$(printf '"%s",' "$@" | sed 's/,$//')

  gh api -X PUT "repos/$ORG/$REPO/branches/$BRANCH/protection" \
    -H "Accept: application/vnd.github+json" \
    --input - <<JSON
{
  "required_status_checks": { "strict": true, "contexts": [ $CONTEXTS ] },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": true,
    "require_last_push_approval": true
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_conversation_resolution": true,
  "required_linear_history": true
}
JSON
}

# App: o nome do check é o `name:` do job em ci.yml (verificado no repo)
protect workshop-service main "Build, test (unit + integration) and coverage"

# Infra: checks de terraform (nomes definidos pelo cicd-pipelines)
protect workshop-infra-kubernetes main "terraform-validate" "tfsec"
protect workshop-infra-database   main "terraform-validate" "tfsec" "rds-not-public"
protect workshop-auth-serverless  main "build-and-test" "terraform-validate"
```

`enforce_admins: true` importa: o usuário é admin da org e sem isso a proteção é decorativa
justamente para quem faria o push direto. O G6 exige "histórico só por PR" — provar isso com
`enforce_admins: false` é frágil.

Se o time adotar `develop` como homologação (recomendado pelo doc 07), proteja `develop`
também, com os mesmos checks e 1 aprovação.

### Verificação / evidência (G1)

```bash
for REPO in workshop-service workshop-infra-kubernetes workshop-infra-database workshop-auth-serverless; do
  echo "== $REPO"
  gh api "repos/$ORG/$REPO" --jq '{visibility, url: .html_url}'
  gh api "repos/$ORG/$REPO/branches/main/protection" \
    --jq '{checks: .required_status_checks.contexts,
           aprovacoes: .required_pull_request_reviews.required_approving_review_count,
           force_push: .allow_force_pushes.enabled,
           admins: .enforce_admins.enabled}' \
    | tee "evidencias/g1-protection-$REPO.json"
done
```

## Padrão: Environments e secrets

Dois Environments por repo que faz deploy: `homolog` e `prod`. Só `prod` tem gate de aprovação —
é esse gate que o G6 exige ver **bloqueando** um apply.

```bash
# Environment de homologação: sem reviewers, deploy automático após merge em develop
gh api -X PUT "repos/$ORG/$REPO/environments/homolog"

# Environment de produção: exige aprovação humana
USER_ID=$(gh api users/<login-do-aprovador> --jq .id)
gh api -X PUT "repos/$ORG/$REPO/environments/prod" --input - <<JSON
{
  "wait_timer": 0,
  "prevent_self_review": true,
  "reviewers": [ { "type": "User", "id": $USER_ID } ],
  "deployment_branch_policy": { "protected_branches": true, "custom_branch_policies": false }
}
JSON
```

`deployment_branch_policy.protected_branches: true` impede que alguém dispare o Environment
`prod` de uma branch qualquer — sem isso, o gate de aprovação existe mas a origem do deploy não.

### Secrets por Environment

```bash
set_env_secret() { gh secret set "$2" --repo "$ORG/$1" --env "$3" --body "$4"; }

for ENV in homolog prod; do
  # AWS Academy: os TRÊS são obrigatórios. A credencial é temporária (~4h) e
  # AWS_SESSION_TOKEN não é opcional — sem ele o provider AWS falha na autenticação.
  set_env_secret "$REPO" AWS_ACCESS_KEY_ID     "$ENV" "$AKID"
  set_env_secret "$REPO" AWS_SECRET_ACCESS_KEY "$ENV" "$SECRET"
  set_env_secret "$REPO" AWS_SESSION_TOKEN     "$ENV" "$TOKEN"

  # JWT: MESMO valor para app e Lambda no MESMO ambiente (doc 07). Valores
  # DIFERENTES entre homolog e prod. Nunca o valor rotacionado-para-fora d3f8a1c2…f2a5.
  set_env_secret "$REPO" JWT_SECRET "$ENV" "$(openssl rand -hex 32)"
done

# Observabilidade (consumido pelo observability-platform)
set_env_secret "$REPO" GRAFANA_CLOUD_OTLP_ENDPOINT prod "$GRAFANA_ENDPOINT"
set_env_secret "$REPO" GRAFANA_CLOUD_INSTANCE_ID   prod "$GRAFANA_INSTANCE"
set_env_secret "$REPO" GRAFANA_CLOUD_API_TOKEN     prod "$GRAFANA_TOKEN"
```

Regras não negociáveis (doc 07):
- **Renovar `AWS_*` imediatamente antes da janela de deploy.** A credencial do Academy expira em
  ~4h; um apply de EKS leva ~15–20 min. Credencial velha = apply pela metade.
- `db_password` **não** trafega por `terraform_remote_state`. Vive como Environment secret,
  consumido igualmente pelo k8s Secret e pela Lambda.
- Nunca `echo`/`grep` de valor de secret em step de workflow. Listar secrets é sempre **por nome**:

```bash
gh secret list --repo "$ORG/$REPO" --env prod   # mostra nomes e data, nunca valores
```

## Padrão: secret scanning e push protection

Em repo público, ambos são gratuitos — mais uma razão para a decisão de visibilidade.

```bash
gh api -X PATCH "repos/$ORG/$REPO" --input - <<'JSON'
{
  "security_and_analysis": {
    "secret_scanning":                  { "status": "enabled" },
    "secret_scanning_push_protection":  { "status": "enabled" }
  },
  "delete_branch_on_merge": true,
  "allow_merge_commit": false,
  "allow_squash_merge": true,
  "allow_rebase_merge": false
}
JSON

gh api "repos/$ORG/$REPO/secret-scanning/alerts" --jq 'length'   # esperado: 0
```

`push_protection` é a rede que evita repetir o achado do JWT: bloqueia o push antes do commit
chegar ao histórico público.

## Gate

### G1 — fundação (W1)
- [ ] Sub-gate de segurança verde nos 4 itens **antes** de qualquer `gh repo create`
- [ ] 4 URLs ativas e públicas
- [ ] Branch protection capturada como evidência (`evidencias/g1-protection-*.json`)
- [ ] Tag `phase3-baseline` criada **antes** da extração do terraform
- [ ] Environments `homolog`/`prod` criados; `prod` com reviewer e `protected_branches`
- [ ] `gh secret list --env prod` mostra `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`,
      `AWS_SESSION_TOKEN`, `JWT_SECRET`, `GRAFANA_*` — só nomes
- [ ] Secret scanning + push protection habilitados, 0 alertas

```bash
# Verificação consolidada do G1
for REPO in workshop-service workshop-infra-kubernetes workshop-infra-database workshop-auth-serverless; do
  gh api "repos/$ORG/$REPO" --jq '"\(.name) \(.visibility) \(.html_url)"'
  gh api "repos/$ORG/$REPO/branches/main/protection" --jq '.required_pull_request_reviews.required_approving_review_count' \
    || echo "  SEM PROTECTION — bloqueia G1"
done
```

### G6 — governança (W6)
- [ ] Required checks apontando para os jobs reais entregues pelo `cicd-pipelines` na W6
- [ ] **Histórico só por PR** nos 4 repos — provar que nenhum commit chegou direto na `main`:

```bash
# Todo commit em main deve ter um PR associado
gh api "repos/$ORG/$REPO/commits?sha=main&per_page=100" --jq '.[].sha' | while read SHA; do
  N=$(gh api "repos/$ORG/$REPO/commits/$SHA/pulls" --jq 'length')
  [ "$N" -eq 0 ] && echo "COMMIT DIRETO: $SHA"
done
gh pr list --repo "$ORG/$REPO" --state merged --limit 20   # ≥1 PR por repo (evidência do doc 07)
```

- [ ] **Gate de aprovação demonstrado bloqueando um apply de prod.** Não é screenshot da
      configuração: é um run real parado. Disparar um `terraform apply` em `prod`, capturar o
      run em estado `waiting`, e só então aprovar:

```bash
gh run list --repo "$ORG/workshop-infra-kubernetes" --workflow terraform-apply.yml --limit 1
gh api "repos/$ORG/workshop-infra-kubernetes/actions/runs/<ID>" --jq '.status'  # "waiting"
gh api "repos/$ORG/workshop-infra-kubernetes/actions/runs/<ID>/pending_deployments" \
  --jq '.[].environment.name'   # "prod"
# capturar a tela do run bloqueado ANTES de aprovar — é essa a evidência do G6
```

- [ ] Evidências guardadas: capturas de branch protection, ≥1 PR por repo, run verde de cada
      CI e de cada deploy, lista de secrets **por nome**

## Como usar este agente
1. **Rodar o sub-gate de segurança primeiro.** Confirmar `application.yml:31`, varrer o histórico com
   `gitleaks`, verificar que `*.tfvars` e `.env` estão ignorados, confirmar 0 k8s Secret real
   versionado. Qualquer achado bloqueia — não publique e reporte.
2. Criar a tag `phase3-baseline` no repo da app, **antes** de o `terraform-cluster` extrair qualquer `.tf`.
3. Criar os 3 repos + `soat-architecture` com `--public`. Semear `.gitignore` (com `*.tfvars`,
   que a raiz do repo atual não tem), README e `*.example` sem valor real.
4. Aplicar branch protection nos 4 repos com `enforce_admins: true`. Os nomes dos required checks
   vêm do `cicd-pipelines` — se os workflows dele ainda não existirem, crie a proteção sem
   contexts e adicione os checks na W6.
5. Criar Environments `homolog`/`prod`; só `prod` com reviewer e `protected_branches`.
6. Popular secrets por Environment. `AWS_SESSION_TOKEN` sempre. `JWT_SECRET` novo e igual entre
   app e Lambda no mesmo ambiente.
7. Habilitar secret scanning + push protection.
8. Capturar as evidências do G1 em JSON e imagem.
9. Na W6, fechar o G6: required checks reais, prova de que a `main` só recebeu PRs, e o run de
   `prod` capturado **em estado bloqueado**.
10. Não escrever `.tf`, `src/`, nem corpo de workflow. Se um required check precisar existir,
    peça ao `cicd-pipelines`.
