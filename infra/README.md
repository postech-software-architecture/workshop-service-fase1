# Infra (Terraform) — `workshop-service`

Provisiona a infraestrutura da Fase 2 via IaC. Duas trilhas:

- **Trilha B (`eks/`) — entrega em nuvem (AWS Academy).** EKS + RDS adaptado ao Academy Learner
  Lab (reusa a `LabRole`, instala metrics-server para o HPA). É a trilha que roda em **nuvem** e
  atende ao direcionamento de usar o AWS Academy. Ver [`eks/README.md`](eks/README.md).
- **Trilha A (raiz) — dev local.** cluster `kind` + Postgres (Helm bitnami) — zero custo, roda na
  máquina/CI. **Não é nuvem** (Kubernetes em Docker): serve para desenvolvimento e validação
  local dos manifestos, não para a publicação no Academy.

## Pré-requisitos

- Docker em execução
- `kind` >= 0.20 (o provider `tehcyx/kind` usa a lib do kind)
- `kubectl`
- `terraform` >= 1.6
- `helm` CLI é **opcional** — o provider Helm do Terraform não exige o binário.

## Contratos (outputs consumidos por outros tracks)

> **Nota:** a tabela abaixo é da **Trilha A (kind local)**. Na **Trilha B (EKS)**, o CD **não** usa
> `KUBECONFIG_B64`: autentica na AWS com credenciais do Academy e roda `aws eks update-kubeconfig`.
> Os outputs de banco (`db_host/port/name/username/password`) têm os **mesmos nomes** nas duas
> trilhas — no EKS apontam para o RDS. Ver [`eks/README.md`](eks/README.md).

| Output | Consumido por | Uso |
|---|---|---|
| `kubeconfig_path` | CD (kind local) | conteúdo em base64 → secret `KUBECONFIG_B64` do GitHub Actions |
| `cluster_name` | CD / deploy | contexto kubectl (`kind-<name>`) |
| `db_host` | Deploy (Secret/Config) | `DB_HOST` (`postgresql.workshop.svc.cluster.local`) |
| `db_port` | Deploy | `DB_PORT` (5432) |
| `db_name` | Deploy | `DB_NAME` |
| `db_username` | Deploy | `DB_USER` |
| `db_password` (sensitive) | Deploy | `DB_PASSWORD` (vai para Secret) |
| `db_jdbc_url` | Deploy | conveniência |

Estes valores correspondem à **Opção B** (banco externo ao Deployment).

## Trilha A — kind + Postgres

```bash
cd infra
cp terraform.tfvars.example terraform.tfvars

terraform init
# Apply em 2 fases: os providers helm/kubernetes dependem do cluster que nasce no mesmo apply.
terraform apply -target=kind_cluster.this -auto-approve   # 1) cria o cluster
terraform apply -auto-approve                             # 2) namespace + Postgres
terraform output
```

Verificar:

```bash
kubectl --kubeconfig ./kubeconfig get nodes                 # node(s) Ready
kubectl --kubeconfig ./kubeconfig -n workshop get pods,svc  # postgresql Running + svc :5432
```

Destruir:

```bash
terraform destroy -auto-approve
kind get clusters   # esperado: sem o cluster workshop
```

## Como os outros tracks consomem

**Dev 2 (CD)** — o kubeconfig gerado vira o secret `KUBECONFIG_B64`:

```bash
base64 -w0 "$(terraform output -raw kubeconfig_path)" | gh secret set KUBECONFIG_B64
```

**Dev 3 (Secret/ConfigMap)** — os outputs alimentam o **mesmo** `workshop-secret` e
`workshop-config` que o Deployment lê (não crie um Secret separado):

```bash
kubectl create secret generic workshop-secret --namespace workshop \
  --from-literal=DB_USER="$(terraform output -raw db_username)" \
  --from-literal=DB_PASSWORD="$(terraform output -raw db_password)" \
  --kubeconfig "$(terraform output -raw kubeconfig_path)" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl create configmap workshop-config --namespace workshop \
  --from-literal=DB_HOST="$(terraform output -raw db_host)" \
  --from-literal=DB_PORT="$(terraform output -raw db_port)" \
  --from-literal=DB_NAME="$(terraform output -raw db_name)" \
  --kubeconfig "$(terraform output -raw kubeconfig_path)" \
  --dry-run=client -o yaml | kubectl apply -f -
```

> O `workshop-secret` também carrega `JWT_SECRET`, `WEBHOOK_ORCAMENTO_TOKEN`, `MAIL_USERNAME`,
> `MAIL_PASSWORD` (contrato Dev 1). Use `kubectl create secret ... | kubectl apply` para mesclar
> as chaves sem sobrescrever as demais.

## Backend do state

- **Entrega (Trilha A):** backend **local** (`terraform.tfstate` em `/infra`, ignorado no git).
- **Evolução:** backend remoto para colaboração/lock — S3 + DynamoDB (AWS) ou GCS (GCP). Ver a
  seção "Backend do state" no README do track (`docs/fase-2/dev-4-terraform-iac/README.md`).

## Trilha B (`eks/`) — EKS + RDS (entrega em nuvem)

Trilha **usada na entrega da Fase 2**: provisiona EKS + RDS no AWS Academy. Gera custo AWS e exige
credenciais temporárias (com `aws_session_token`).

```bash
cd eks
cp terraform.tfvars.example terraform.tfvars   # define db_password (não commitar)
terraform init
terraform apply                                # VPC + EKS (LabRole) + RDS + metrics-server
aws eks update-kubeconfig --name workshop-eks --region us-east-1
```

O deploy dos manifestos é feito pela pipeline **CD** (`.github/workflows/cd.yml`) — que autentica
na AWS, cria o `workshop-secret` a partir dos GitHub Secrets e aplica `k8s/overlays/aws` — ou
manualmente via `kubectl apply -k ../k8s/overlays/aws`.

> No **AWS Academy** (sandbox de crédito limitado), derruba-se o ambiente com `terraform destroy`
> após a demonstração. Isso é uma restrição do lab, **não** parte do deploy — em produção a infra
> permanece de pé. Passo a passo e credenciais em [`eks/README.md`](eks/README.md).

Validação estática (sem apply/credenciais): `terraform init -backend=false && terraform validate`.
