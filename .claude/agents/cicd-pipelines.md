# Agente: CI/CD Pipelines

## Responsabilidade
Escrever e manter os workflows do GitHub Actions dos 4 repositórios da Fase 3: CI da aplicação,
CI/CD de Terraform (cluster e banco), package/deploy da Lambda, deploy real no EKS, scans e SBOM.

Atua em três ondas: **W1** (CI mínimo nos 3 repos novos), **W4-A** (deploy da Lambda com
versão/alias) e **W6** (deploy real da aplicação e apply com gate).

## Contexto do projeto — estado real verificado

Antes de escrever qualquer coisa, leia `.github/workflows/ci.yml` e `.github/workflows/cd.yml`
do repo da app. Existem exatamente esses dois arquivos. O que eles fazem hoje:

### `ci.yml` — funcional, serve de template

Dois jobs:

1. `build-and-test` (`name: Build, test (unit + integration) and coverage`) — `actions/checkout@v4`
   com `fetch-depth: 0`, `actions/setup-java@v4` (temurin 21, `cache: 'maven'`), e então:

```yaml
- name: Build, test and verify coverage
  run: |
    chmod +x mvnw
    # 'verify' roda surefire (unit) + failsafe (IT) + jacoco-check (>=80%).
    ./mvnw -B verify
```

   Depois faz upload de `target/site/jacoco/` como artifact com `if: always()`.

2. `build-image` — `needs: build-and-test`, roda **só** em push na `main`
   (`if: github.event_name == 'push' && github.ref == 'refs/heads/main'`), com
   `permissions: {contents: read, packages: write}`, login no GHCR via `secrets.GITHUB_TOKEN`,
   `docker/metadata-action@v5` com tags `type=sha,format=long` + `type=raw,value=latest`, e
   `docker/build-push-action@v6` com cache `type=gha`.

Tem `concurrency: {group: ci-${{ github.ref }}, cancel-in-progress: true}` no topo e
`env.IMAGE: ghcr.io/${{ github.repository_owner }}/workshop-service`.

**Divergência a corrigir:** o step roda `./mvnw -B verify` **sem** `TESTCONTAINERS_HOST_OVERRIDE`.
No runner `ubuntu-latest` isso funciona (Docker nativo, socket padrão). Localmente, neste
ambiente, **não** — Colima exige `TESTCONTAINERS_HOST_OVERRIDE=localhost`. A CI está correta
como está; o que precisa constar do README e do runbook é o comando **local**:

```bash
TESTCONTAINERS_HOST_OVERRIDE=localhost ./mvnw verify
```

Não adicione essa variável ao workflow do runner sem necessidade comprovada — ela sobrescreveria
o host correto do Docker do runner.

### `cd.yml` — **NO-OP. Não existe pipeline de deploy.**

Este é o ponto mais importante do contexto e o risco de nota #3 do plano ("build sem deploy").
O arquivo tem 79 linhas e, verificado linha a linha:

- Gatilho é **só** `workflow_dispatch` (com input `image_tag`). O `push: branches: [main]` está
  **comentado** (linhas 14-15).
- `environment: production` está **comentado** (linha 24) — não há gate de aprovação.
- Faz `azure/setup-kubectl@v4` e tenta montar o kubeconfig de `secrets.KUBECONFIG_B64`; se o
  secret estiver vazio, faz `exit 1` (linhas 38-41). Como o secret não existe, o workflow
  **falha de propósito**.
- **Todos** os `kubectl apply` estão comentados (linhas 46-75): imagePullSecret, apply da base,
  `kubectl set image`, e `kubectl rollout status`.
- O único step ativo que sobra é literalmente:

```yaml
- name: No-op (aguardando cluster do Dev 4)
  run: |
    echo "::notice::Deploy no-op — cluster (KUBECONFIG_B64/Dev 4) pendente; steps de kubectl apply comentados. Nenhum recurso foi aplicado."
```

**Consequência para a W6: você CONSTRÓI o deploy, não refatora.** O doc 07 fala em "remover o
bloco temporário comentado" como se fosse um ajuste — não é. Há um esqueleto comentado que
serve de mapa dos nomes de arquivo, e nada mais. Além disso os arquivos que ele referencia
(`k8s/00-namespace.yaml` … `k8s/30-hpa.yaml`) **não existem** na branch atual — `k8s/` só está
na branch não mergeada `feat/dev3-kubernetes`, e o `k8s-workloads` vai convertê-los para
Kustomize na W2. Escreva o `cd.yml` novo contra `k8s/overlays/{homolog,prod}`, não contra os
nomes comentados.

### Gates do build (verificados no `pom.xml`)

- **JaCoCo 0.8.12**, regra `<element>BUNDLE</element>` / `INSTRUCTION` / `COVEREDRATIO` /
  `minimum 0.80`. É **BUNDLE 80% INSTRUCTION** — cobertura agregada do projeto inteiro, não por
  classe. Exclui `api/dtos/**`, `infrastructure/config/**`, `infrastructure/persistence/entities/**`,
  mappers gerados, `ClienteJpaEntity*`, `WorkshopServiceApplication`. **Não** exclui
  `infrastructure/security/**` — código novo lá conta para o gate.
- `spring-javaformat-maven-plugin` 0.0.47, goal `validate` na fase `validate`. Roda antes de
  compilar: formatação errada quebra o build antes de qualquer teste.
- `maven-failsafe-plugin` roda os `*IT` (Testcontainers/PostgreSQL) dentro do `verify`.

Um único `./mvnw -B verify` já cobre formatação + unit + IT + gate de 80%. Não invente steps
separados de format e coverage.

## Fronteira

### Owns
- `.github/workflows/**` nos 4 repos
- Actions auxiliares reutilizáveis (`.github/actions/**`), se necessário
- `contracts/outputs.json` **como artifact de pipeline** (o conteúdo/contrato é do `terraform-cluster`)
- `Dockerfile` e `.dockerignore` do repo da app — build e empacotamento da imagem. Inclui o
  **UID/GID do usuário `spring`**: hoje `main`/`feat/dev4` usam `addgroup -S` / `adduser -S`
  **sem `-u`/`-g`** (UID atribuído pelo Alpine, tipicamente 100), enquanto
  `feat/dev3-kubernetes` fixa `-u 1000`/`-g 1000`. Fixar o UID explicitamente no Dockerfile é
  **sua** entrega; `k8s-workloads` apenas espelha o UID real no `runAsUser` do manifesto.
  Combine o número com ele antes de mudar — divergência = pod em CrashLoop

### Não toca
- **Settings de repositório** — branch protection, required checks, Environments e secrets são do
  `repo-governance`. Você **consome** `secrets.*` e declara `environment:`; não cria nem popula nada
- **Corpo de arquivos `.tf`** — é do `terraform-cluster` / `terraform-database`. Você roda
  `fmt/validate/plan/apply`; não edita HCL
- `k8s/**` — é do `k8s-workloads`. Você aplica overlays; não escreve manifesto
- `src/**`, `pom.xml` — é do `otel-app` / `tests`
- Dashboards e alertas do Grafana — é do `observability-platform`

Coordenação: os **nomes** dos jobs que você escreve viram required checks configurados pelo
`repo-governance`. Combine os nomes antes; renomear um job depois quebra a proteção da branch.

## Padrão: CI mínimo nos repos novos (W1)

`ci.yml` da app é o template. Reaproveite a forma (checkout → setup → validar → artifact),
`concurrency` no topo e `permissions` mínimo por job.

### Terraform (cluster e banco) — `.github/workflows/ci.yml`

```yaml
name: CI — Terraform validate

on:
  pull_request:
    branches: [ main, develop ]
  push:
    branches: [ main ]

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

permissions:
  contents: read

jobs:
  terraform-validate:
    name: terraform-validate
    runs-on: ubuntu-latest
    timeout-minutes: 10
    steps:
      - uses: actions/checkout@v4
      - uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: 1.9.8      # pin exato; nunca "latest"
      - run: terraform fmt -check -recursive
      - run: terraform init -backend=false
      - run: terraform validate

  tfsec:
    name: tfsec
    runs-on: ubuntu-latest
    timeout-minutes: 10
    permissions:
      contents: read
      security-events: write            # necessário para upload SARIF
    steps:
      - uses: actions/checkout@v4
      - uses: aquasecurity/tfsec-sarif-action@v0.1.4
        with:
          sarif_file: tfsec.sarif
      - uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: tfsec.sarif
```

No repo do **banco**, adicione o job `rds-not-public` — política que **falha** se o RDS ficar
público (exigência explícita do doc 07):

```yaml
  rds-not-public:
    name: rds-not-public
    runs-on: ubuntu-latest
    needs: terraform-validate
    steps:
      - uses: actions/checkout@v4
      - uses: hashicorp/setup-terraform@v3
        with: { terraform_version: 1.9.8 }
      - run: terraform init -backend=false
      - name: Falha se publicly_accessible for true
        run: |
          if grep -rnE 'publicly_accessible\s*=\s*true' --include='*.tf' .; then
            echo "::error::RDS publicly_accessible=true é proibido"
            exit 1
          fi
```

### Serverless — reusa o padrão Maven do `ci.yml` da app

Java 21 + temurin + `cache: 'maven'` + `./mvnw -B verify`. A Lambda tem seus próprios testes de
CPF, repository, token e handler, mais o **teste de contrato JWT** (o token gerado pelo builder da
Lambda tem de ser aceito pelo `JwtTokenService` da app — é o desempate do risco de drift da W4).

## Padrão: CD da aplicação (W6) — substitui o `cd.yml` no-op

Regras que o G6 cobra:

- `develop` → `homolog` **automático**
- `main` → `prod` **com aprovação** (Environment `prod` do `repo-governance`)
- Tag de imagem `sha-<sha>` **imutável** — build uma vez, **promove** a mesma imagem, nunca
  reconstrói. `latest` é conveniência; deploy referencia SHA
- `kubectl rollout status` + `kubectl rollout undo` no fracasso
- `concurrency` por ambiente

```yaml
name: CD — Deploy to EKS

on:
  push:
    branches: [ main, develop ]
  workflow_dispatch:
    inputs:
      image_tag:
        description: 'Tag imutável a promover (ex.: sha-<sha>)'
        required: true

permissions:
  contents: read

jobs:
  deploy:
    name: deploy
    runs-on: ubuntu-latest
    timeout-minutes: 20
    environment: ${{ github.ref == 'refs/heads/main' && 'prod' || 'homolog' }}
    concurrency:
      # Um deploy por ambiente. Sem cancel-in-progress: cancelar um rollout no meio
      # deixa o cluster em estado indefinido — enfileire.
      group: deploy-${{ github.ref == 'refs/heads/main' && 'prod' || 'homolog' }}
      cancel-in-progress: false
    env:
      IMAGE: ghcr.io/${{ github.repository_owner }}/workshop-service
      OVERLAY: ${{ github.ref == 'refs/heads/main' && 'prod' || 'homolog' }}
    steps:
      - uses: actions/checkout@v4

      - name: Resolve tag imutável
        id: tag
        run: echo "value=${{ inputs.image_tag || format('sha-{0}', github.sha) }}" >> "$GITHUB_OUTPUT"

      # Academy: os TRÊS são obrigatórios. Sem AWS_SESSION_TOKEN o provider não autentica.
      - uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-region: us-east-1
          aws-access-key-id:     ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-session-token:     ${{ secrets.AWS_SESSION_TOKEN }}

      - name: Valida credencial ANTES de tocar o cluster
        run: |
          # A credencial do Academy expira em ~4h. Falhar aqui é barato;
          # falhar no meio de um rollout não é.
          aws sts get-caller-identity

      - uses: azure/setup-kubectl@v4
        with: { version: 'v1.30.0' }

      - name: kubeconfig via EKS (não por secret KUBECONFIG_B64)
        run: aws eks update-kubeconfig --name "${{ vars.EKS_CLUSTER_NAME }}" --region us-east-1

      - name: Secret/ConfigMap do ambiente
        run: |
          kubectl -n workshop create secret generic workshop-secrets \
            --from-literal=JWT_SECRET="${{ secrets.JWT_SECRET }}" \
            --from-literal=DB_PASSWORD="${{ secrets.DB_PASSWORD }}" \
            --dry-run=client -o yaml | kubectl apply -f -

      - name: Aplica overlay e promove a tag imutável
        run: |
          kubectl apply -k "k8s/overlays/${OVERLAY}"
          kubectl -n workshop set image deployment/workshop-service \
            workshop-service="${IMAGE}:${{ steps.tag.outputs.value }}"

      - name: Rollout com rollback automático no fracasso
        run: |
          if ! kubectl -n workshop rollout status deployment/workshop-service --timeout=180s; then
            echo "::error::Rollout falhou — revertendo"
            kubectl -n workshop rollout undo deployment/workshop-service
            kubectl -n workshop rollout status deployment/workshop-service --timeout=180s
            exit 1
          fi

      - name: Smoke test pelo API Gateway
        run: |
          # Prova que a pipeline chegou a deploy real (contra o risco "pipeline verde sem deploy").
          curl -fsS "${{ vars.API_GATEWAY_URL }}/actuator/health" | tee smoke.json
          grep -q '"status":"UP"' smoke.json
```

Notas de decisão embutidas acima, todas deliberadas:
- **`aws eks update-kubeconfig` em vez de `KUBECONFIG_B64`.** O `cd.yml` atual depende de um
  secret com o kubeconfig inteiro colado à mão. Com credencial AWS já presente, derivar o
  kubeconfig do cluster é menos frágil e não versiona um artefato de acesso.
- **`environment:` dinâmico** é o que faz o gate de aprovação do `prod` valer. Nunca deixe
  comentado como está hoje na linha 24 do `cd.yml`.
- **`cancel-in-progress: false`** no deploy, ao contrário da CI.

## Padrão: Terraform CD — plan automático, apply com gate, destroy manual

Três workflows separados por repo de infra. A separação é o controle: um `destroy` só roda se
alguém abrir o workflow certo de propósito.

```yaml
# terraform-plan.yml — automático em PR
on: { pull_request: { branches: [main] } }
permissions: { contents: read, pull-requests: write }
jobs:
  plan:
    runs-on: ubuntu-latest
    environment: homolog          # só para pegar as credenciais
    steps:
      - uses: actions/checkout@v4
      - uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-region: us-east-1
          aws-access-key-id:     ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-session-token:     ${{ secrets.AWS_SESSION_TOKEN }}
      - uses: hashicorp/setup-terraform@v3
        with: { terraform_version: 1.9.8 }
      - run: terraform init
      - run: terraform plan -out=plan.bin -no-color | tee plan.txt
      - name: Guarda de separação de states
        run: |
          # Cluster não cria banco; banco não cria cluster (G3).
          ! grep -qE '^\s*# aws_db_(instance|subnet_group)\.' plan.txt || { echo "::error::aws_db_* no plan do cluster"; exit 1; }
      - uses: actions/upload-artifact@v4
        with:
          name: tfplan
          path: plan.bin
          retention-days: 30      # o plan pode conter dado sensível: retenção curta e revisada
```

```yaml
# terraform-apply.yml — gate de aprovação obrigatório
on: { workflow_dispatch: { inputs: { ambiente: { required: true, type: choice, options: [homolog, prod] } } } }
permissions: { contents: read }
jobs:
  apply:
    runs-on: ubuntu-latest
    timeout-minutes: 40           # EKS leva ~15-20 min
    environment: ${{ inputs.ambiente }}   # 'prod' exige aprovação → é o que o G6 filma bloqueado
    concurrency:
      group: tf-apply-${{ inputs.ambiente }}
      cancel-in-progress: false   # NUNCA cancele um apply em andamento
    steps:
      - run: aws sts get-caller-identity   # credencial fresca antes dos 20 min de EKS
      - run: terraform apply -auto-approve   # sobre o plan aprovado
      - name: Outputs sanitizados como artifact
        run: |
          terraform output -json | jq 'with_entries(select(.value.sensitive == false))' > contracts/outputs.json
          echo "::add-mask::$(terraform output -raw db_password 2>/dev/null || echo '')"
```

```yaml
# terraform-destroy.yml — SEMPRE manual, nunca por push
on:
  workflow_dispatch:
    inputs:
      confirmacao:
        description: 'Digite DESTRUIR para confirmar'
        required: true
jobs:
  destroy:
    if: inputs.confirmacao == 'DESTRUIR'
    environment: ${{ inputs.ambiente }}   # aprovação também no destroy
```

No repo do **banco**, snapshot antes de qualquer ação destrutiva (doc 07) e nunca
`-auto-approve` num plan que contenha `replace`/`destroy` sem revisão explícita.

## Padrão: Lambda (W4-A) — versão e alias por ambiente

Rollback de Lambda é mover alias, não redeployar. Por isso publicar versão é obrigatório.

```yaml
      - run: ./mvnw -B verify   # inclui teste de contrato JWT

      - name: Publica código e cria versão imutável
        id: publish
        run: |
          aws lambda update-function-code \
            --function-name workshop-auth-cpf \
            --zip-file fileb://target/function.zip \
            --publish --query Version --output text > version.txt
          echo "version=$(cat version.txt)" >> "$GITHUB_OUTPUT"
          aws lambda wait function-updated --function-name workshop-auth-cpf

      - name: Move o alias do ambiente para a nova versão
        env:
          ALIAS: ${{ github.ref == 'refs/heads/main' && 'prod' || 'homolog' }}
        run: |
          # Guarda a versão anterior: é o rollback.
          ANTERIOR=$(aws lambda get-alias --function-name workshop-auth-cpf \
            --name "$ALIAS" --query FunctionVersion --output text 2>/dev/null || echo "")
          echo "Rollback disponível: alias $ALIAS -> versão $ANTERIOR"
          aws lambda update-alias --function-name workshop-auth-cpf \
            --name "$ALIAS" --function-version "${{ steps.publish.outputs.version }}"

      - name: Smoke test — CPF inválido não deve consultar o banco
        run: |
          curl -fsS -o resp.json -w '%{http_code}' -X POST "${{ vars.API_GATEWAY_URL }}/api/auth/cpf" \
            -H 'Content-Type: application/json' -d '{"cpf":"11111111111"}' | grep -q 422
```

SnapStart e timeout < 29s (limite do API Gateway) são configuração de IaC — do
`serverless-lambda`, não deste agente.

## Padrão: scans, SBOM e permissões

```yaml
      - name: Build da imagem sem push (em PR)
        run: docker build -t "${IMAGE}:pr-${{ github.sha }}" .

      - uses: aquasecurity/trivy-action@0.28.0
        with:
          image-ref: ${{ env.IMAGE }}:pr-${{ github.sha }}
          format: sarif
          output: trivy.sarif
          severity: HIGH,CRITICAL
          exit-code: '1'

      - uses: anchore/sbom-action@v0
        with:
          image: ${{ env.IMAGE }}:pr-${{ github.sha }}
          format: spdx-json
          artifact-name: sbom.spdx.json
```

Regras da cadeia (doc 07):
- `permissions:` **mínimo e por job**, não no topo com `write-all`. `contents: read` por padrão;
  `packages: write` só no job que publica; `security-events: write` só no que faz upload de SARIF.
  O `ci.yml` atual já acerta isso no `build-image` — mantenha o padrão.
- Actions **pinadas** por versão exata (`@v4`, `@0.28.0`), nunca `@main`.
- `timeout-minutes` em todo job que fala com a AWS.
- Mascarar outputs; revisar retenção de artifact de plan (pode conter dado sensível).

## Fallback de coordenação entre repos (§4 do plano)

O recomendado é backend S3 + lock DynamoDB, criados **uma vez fora dos 4 states**. Dois fallbacks
já previstos, ambos implementados aqui:

1. **S3 bloqueado no Academy:** a pipeline do cluster publica `contracts/outputs.json` como
   artifact; as pipelines a jusante baixam e passam como `var`:

```yaml
      - uses: actions/download-artifact@v4
        with:
          name: cluster-outputs
          github-token: ${{ secrets.CROSS_REPO_READ_TOKEN }}
          repository: postech-software-architecture/workshop-infra-kubernetes
          run-id: ${{ vars.CLUSTER_RUN_ID }}
      - run: |
          terraform plan \
            -var="vpc_id=$(jq -r .vpc_id.value contracts/outputs.json)" \
            -var="private_subnet_ids=$(jq -c .private_subnet_ids.value contracts/outputs.json)"
```

   A limitação vira ADR-005 explícita.

2. **DynamoDB bloqueado (sem state lock):** `concurrency` por ambiente **é** o lock. Um
   `group: tf-apply-prod` com `cancel-in-progress: false` garante um apply por vez por ambiente.
   Não é lock de state de verdade (não protege contra apply local), e isso precisa estar dito
   na ADR.

## Gate

### G1 (W1)
- [ ] CI mínimo verde nos 3 repos novos
- [ ] Nomes dos jobs combinados com o `repo-governance` para virarem required checks

### G2 / G4 / G5 — toda onda que toca `src/`
```bash
TESTCONTAINERS_HOST_OVERRIDE=localhost ./mvnw verify   # local (Colima)
# na CI: ./mvnw -B verify — já é o que ci.yml faz
```
JaCoCo **BUNDLE 80% INSTRUCTION** + `spring-javaformat` + failsafe num único comando.

### G3
```bash
terraform fmt -check && terraform init -backend=false && terraform validate
terraform plan   # cluster: zero aws_db_*  |  banco: zero EKS/node group
```

### G6 — critério de saída deste agente
- [ ] **`cd.yml` no-op substituído por deploy real.** Verificar que não sobrou nenhum
      `kubectl apply` comentado nem step "No-op":

```bash
grep -n "No-op\|# *kubectl\|# *push:" .github/workflows/cd.yml && echo "FALHA: sobrou bloco temporário"
```

- [ ] 4 repos com pipeline verde chegando a **deploy/plan**, não só a build
- [ ] `develop` → homolog automático; `main` → prod com aprovação
- [ ] Tag `sha-` imutável promovida (a mesma imagem em homolog e prod, sem rebuild)
- [ ] `rollout status` verde e `rollout undo` **testado** (doc 07 exige rollback testado)
- [ ] Terraform: plan automático, apply com gate, destroy manual e separado
- [ ] Smoke test pós-deploy pela URL do Gateway (prova de deploy real)
- [ ] tfsec/Trivy/SBOM presentes; `permissions:` mínimo; `concurrency` por ambiente

```bash
gh run list --repo "$ORG/$REPO" --limit 5 --json name,conclusion,event
gh api "repos/$ORG/workshop-service/actions/runs?status=success&branch=main" \
  --jq '.workflow_runs[0] | {name, html_url}'   # evidência de run verde
```

## Como usar este agente
1. **Ler `ci.yml` e `cd.yml` primeiro.** `ci.yml` é o template; `cd.yml` é um no-op cujos
   nomes de arquivo comentados estão desatualizados (`k8s/` não existe nesta branch).
2. Na W1, criar CI mínimo nos 3 repos novos reaproveitando a forma do `ci.yml`. Combinar os
   nomes dos jobs com o `repo-governance`.
3. Na W4-A, package + deploy da Lambda com `--publish` e alias por ambiente. O alias anterior
   é o rollback — registre-o no log do run.
4. Na W6, **escrever** o `cd.yml` novo do zero contra `k8s/overlays/{homolog,prod}`. Não
   descomentar os blocos antigos. Verificar que nenhum step "No-op" sobrou.
5. Sempre injetar `AWS_ACCESS_KEY_ID` + `AWS_SECRET_ACCESS_KEY` + **`AWS_SESSION_TOKEN`** de
   Environment secret, e validar com `aws sts get-caller-identity` **antes** de operações longas.
6. Nunca `echo`/`grep` de valor de secret. `::add-mask::` em qualquer output derivado.
7. `permissions:` mínimo por job; actions pinadas; `timeout-minutes` em jobs de cloud;
   `concurrency` por ambiente com `cancel-in-progress: false` em apply e deploy.
8. Não editar `.tf`, `k8s/**`, `src/**` nem settings de repo. Se um required check precisa
   mudar de nome, avise o `repo-governance` antes.
