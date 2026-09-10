# Overlay `aws` — deploy no EKS (Academy) apontando para o RDS

Diferente do `dev`, aqui **não** há Postgres in-cluster: o banco é o **RDS** provisionado
no repositório
[`workshop-infra-database`](https://github.com/postech-software-architecture/workshop-infra-database).
O cluster e a rede são mantidos em
[`workshop-infra-kubernetes`](https://github.com/postech-software-architecture/workshop-infra-kubernetes).
Os valores dinâmicos precisam ser configurados na hora do deploy:

## 1. Injetar o endpoint do RDS no ConfigMap

Obtenha `db_host` no state do repositório `workshop-infra-database` e configure o secret
`DB_HOST` no GitHub Actions. A pipeline de CD injeta esse valor em `config.env` antes de
aplicar o overlay. Em um deploy manual:

```bash
export DB_HOST='<endpoint-do-rds>'
sed -i "s|^DB_HOST=.*|DB_HOST=${DB_HOST}|" k8s/overlays/aws/config.env
```

## 2. Criar o Secret com as credenciais do banco (não vai ao git)

```bash
export DB_USER='<usuario-do-rds>'
export DB_PASSWORD='<senha-segura-usada-no-provisionamento>'
kubectl -n workshop create secret generic workshop-secret \
  --from-literal=DB_USER="$DB_USER" \
  --from-literal=DB_PASSWORD="$DB_PASSWORD" \
  --from-literal=JWT_SECRET="$(openssl rand -hex 32)" \
  --from-literal=WEBHOOK_ORCAMENTO_TOKEN="$(openssl rand -hex 16)" \
  --from-literal=MAIL_USERNAME=dummy \
  --from-literal=MAIL_PASSWORD=dummy
```

O usuário pode ser consultado pelo output `db_username`. A senha não é exposta como output
Terraform; use o mesmo valor seguro fornecido ao provisionar o RDS.

> O namespace precisa existir antes: `kubectl create namespace workshop` (ou aplique o
> overlay uma vez para criá-lo e recrie o secret em seguida).

## 3. Aplicar

```bash
kubectl apply -k k8s/overlays/aws
kubectl -n workshop rollout status deployment/workshop-service
```

> Imagem privada no GHCR → `ImagePullBackOff`. Torne o pacote público ou crie um
> `imagePullSecret` (`ghcr-pull`) e referencie no Deployment (ver `k8s/README.md`).
