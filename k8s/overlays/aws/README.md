# Overlay `aws` — deploy no EKS (Academy) apontando para o RDS

Diferente do `dev`, aqui **não** há Postgres in-cluster: o banco é o **RDS** provisionado
pelo Terraform (`infra/eks`). Dois passos que dependem do `terraform apply` precisam ser
feitos **na hora do deploy**, porque os valores são dinâmicos:

## 1. Injetar o endpoint do RDS no ConfigMap

```bash
cd infra/eks
RDS_HOST=$(terraform output -raw db_host)         # endpoint do RDS
cd ../../
sed -i '' "s|^DB_HOST=.*|DB_HOST=${RDS_HOST}|" k8s/overlays/aws/config.env
```

## 2. Criar o Secret a partir dos outputs do Terraform (não vai ao git)

```bash
cd infra/eks
kubectl -n workshop create secret generic workshop-secret \
  --from-literal=DB_USER="$(terraform output -raw db_username)" \
  --from-literal=DB_PASSWORD="$(terraform output -raw db_password)" \
  --from-literal=JWT_SECRET="$(openssl rand -hex 32)" \
  --from-literal=WEBHOOK_ORCAMENTO_TOKEN="$(openssl rand -hex 16)" \
  --from-literal=MAIL_USERNAME=dummy \
  --from-literal=MAIL_PASSWORD=dummy
```

> O namespace precisa existir antes: `kubectl create namespace workshop` (ou aplique o
> overlay uma vez para criá-lo e recrie o secret em seguida).

## 3. Aplicar

```bash
kubectl apply -k k8s/overlays/aws
kubectl -n workshop rollout status deployment/workshop-service
```

> Imagem privada no GHCR → `ImagePullBackOff`. Torne o pacote público ou crie um
> `imagePullSecret` (`ghcr-pull`) e referencie no Deployment (ver `k8s/README.md`).
