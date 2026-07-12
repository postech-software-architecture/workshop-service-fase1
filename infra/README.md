# Infra (Terraform) — `workshop-service`

Provisiona a infraestrutura da Fase 2 via IaC. **Trilha A** (recomendada, entrega): cluster
`kind` + Postgres (chart bitnami) via Helm — zero custo, local. **Trilha B** (`eks/`): esqueleto
EKS + RDS, só documentado (não aplicar).

## Pré-requisitos

- Docker em execução
- `kind` >= 0.20 (o provider `tehcyx/kind` usa a lib do kind)
- `kubectl`
- `terraform` >= 1.6
- `helm` CLI é **opcional** — o provider Helm do Terraform não exige o binário.

## Contratos (outputs consumidos por outros tracks)

| Output | Consumido por | Uso |
|---|---|---|
| `kubeconfig_path` | Dev 2 (CD) | conteúdo em base64 → secret `KUBECONFIG_B64` do GitHub Actions |
| `cluster_name` | Dev 2/3 | contexto kubectl (`kind-<name>`) |
| `db_host` | Dev 3 | `DB_HOST` (`postgresql.workshop.svc.cluster.local`) |
| `db_port` | Dev 3 | `DB_PORT` (5432) |
| `db_name` | Dev 3 | `DB_NAME` |
| `db_username` | Dev 3 | `DB_USER` |
| `db_password` (sensitive) | Dev 3 | `DB_PASSWORD` (vai para Secret) |
| `db_jdbc_url` | Dev 3 | conveniência |

Estes valores correspondem à **Opção B** do Dev 3 (banco externo ao Deployment).

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

## Trilha B (`eks/`) — EKS + RDS (NÃO aplicar)

Esqueleto de nuvem, gera custo AWS e exige credenciais. Só validação estática:

```bash
cd eks
terraform init -backend=false
terraform validate
```
Ver `eks/README.md` para custo/credenciais.
