# Infra (Terraform) — `workshop-service`

Provisiona o ambiente de desenvolvimento local via IaC: cluster `kind` + Postgres
(Helm bitnami), sem custo de nuvem. Kubernetes em Docker serve para desenvolvimento e
validação local dos manifestos.

A infraestrutura AWS tem ciclo de vida e state próprios:

- [`workshop-infra-kubernetes`](https://github.com/postech-software-architecture/workshop-infra-kubernetes): VPC, EKS, node group e add-ons.
- [`workshop-infra-database`](https://github.com/postech-software-architecture/workshop-infra-database): RDS PostgreSQL.

## Pré-requisitos

- Docker em execução
- `kind` >= 0.20 (o provider `tehcyx/kind` usa a lib do kind)
- `kubectl`
- `terraform` >= 1.6
- `helm` CLI é **opcional** — o provider Helm do Terraform não exige o binário.

## Contratos (outputs consumidos por outros tracks)

> **Nota:** a tabela abaixo descreve somente o ambiente `kind` local. No EKS, o CD autentica na
> AWS com credenciais do Academy e roda `aws eks update-kubeconfig`; consulte os repositórios
> de infraestrutura acima para seus contratos de outputs.

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

## kind + Postgres

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

## EKS + RDS (entrega em nuvem)

O provisionamento em nuvem foi extraído deste repositório. Siga os READMEs de
`workshop-infra-kubernetes` e `workshop-infra-database`, nessa ordem. A pipeline
`.github/workflows/cd.yml` permanece responsável por aplicar `k8s/overlays/aws` no cluster.
