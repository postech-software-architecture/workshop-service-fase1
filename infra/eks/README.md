# Trilha B — EKS + RDS no AWS Academy (entrega em nuvem)

Provisiona o ambiente de nuvem da entrega: cluster **EKS** + **RDS Postgres**, adaptado ao
**AWS Academy Learner Lab**. Expõe os **mesmos contratos** de output da Trilha A
(`db_host/port/name/username/password`, `cluster_name`), então o Dev 3 consome sem alteração.

## Por que adaptado ao Academy

O Academy Learner Lab **bloqueia criação de IAM** — só a role pré-existente `LabRole` é usável.
Por isso este código **não cria roles**:

- `data "aws_iam_role" "lab"` referencia a `LabRole`.
- `module.eks` usa `create_iam_role = false` + `iam_role_arn = LabRole` (no cluster **e** no node group).
- A role do lab é mapeada como admin do cluster via **EKS access entry**
  (`AmazonEKSClusterAdminPolicy`), senão o cluster sobe mas `kubectl` não acessa.
- Um `helm_release "metrics-server"` é instalado — **o HPA do Dev 3 depende dele** para ler CPU.
- Região `us-east-1` (default do Academy).

## Pré-requisitos

- Terraform >= 1.6, `kubectl`, `aws` CLI.
- **Credenciais do Academy** (o Learner Lab dá credenciais temporárias, expiram ~4h):
  no lab, botão **"AWS Details" → "AWS CLI"**, copie o bloco para `~/.aws/credentials` — ele
  inclui `aws_access_key_id`, `aws_secret_access_key` e **`aws_session_token` (obrigatório)**:

  ```ini
  [default]
  aws_access_key_id=ASIA...
  aws_secret_access_key=...
  aws_session_token=...
  ```

  Ou via env: `export AWS_ACCESS_KEY_ID=... AWS_SECRET_ACCESS_KEY=... AWS_SESSION_TOKEN=...`

## Fluxo de entrega (para o vídeo)

```bash
# 1) credenciais do Academy exportadas (com session token)
cd infra/eks
terraform init
terraform apply                     # cria VPC + EKS (LabRole) + RDS + metrics-server
aws eks update-kubeconfig --name workshop-eks --region us-east-1

# 2) Secret com as credenciais do RDS (nao vai ao git) + manifestos via Kustomize
#    (o metrics-server ja foi instalado pelo terraform)
kubectl create namespace workshop --dry-run=client -o yaml | kubectl apply -f -
kubectl -n workshop create secret generic workshop-secret \
  --from-literal=DB_USER="$(terraform output -raw db_username)" \
  --from-literal=DB_PASSWORD="$(terraform output -raw db_password)" \
  --from-literal=JWT_SECRET="$(openssl rand -hex 32)" \
  --from-literal=WEBHOOK_ORCAMENTO_TOKEN="$(openssl rand -hex 16)" \
  --from-literal=MAIL_USERNAME=dummy --from-literal=MAIL_PASSWORD=dummy \
  --dry-run=client -o yaml | kubectl apply -f -
# injeta o endpoint do RDS no ConfigMap do overlay e aplica
sed -i '' "s|^DB_HOST=.*|DB_HOST=$(terraform output -raw db_host)|" ../../k8s/overlays/aws/config.env
kubectl apply -k ../../k8s/overlays/aws

# 3) gerar carga e mostrar a escala automatica
kubectl -n workshop get hpa -w      # REPLICAS sobem sob carga (HPA por CPU)
kubectl -n workshop get pods -w

# 4) destruir tudo depois de gravar (remova o Service LoadBalancer antes p/ o ELB nao ficar orfao)
kubectl delete -k ../../k8s/overlays/aws
terraform destroy
```

> Na entrega, os passos 2–3 são feitos pela pipeline **CD** (`.github/workflows/cd.yml`), que já
> autentica na AWS, cria o `workshop-secret` a partir dos GitHub Secrets, injeta o `DB_HOST` do
> RDS e aplica o overlay `aws`. O `db_host` do RDS é o endpoint do `aws_db_instance.postgres`.

## Custo (crédito ~US$100 do lab)

Control plane EKS ~US$0,10/h + 2× EC2 t3.medium + RDS db.t3.micro + NAT Gateway (~US$0,045/h)
+ tráfego → poucos dólares por algumas horas. **Sempre `terraform destroy` no fim.** Manter o
node group pequeno (1–2 nós). A senha do banco usa `var.db_password`; em produção real, use
`aws_secretsmanager_secret`.

## Validação estática (sem apply / sem credenciais)

```bash
cd infra/eks
terraform init -backend=false
terraform validate
```
