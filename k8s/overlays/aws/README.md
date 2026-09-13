# Overlay `aws` — deploy no EKS (Academy) apontando para o RDS

Diferente do `dev`, aqui **não** há Postgres in-cluster: o banco é o **RDS** provisionado
no repositório
[`workshop-infra-database`](https://github.com/postech-software-architecture/workshop-infra-database).
O cluster e a rede são mantidos em
[`workshop-infra-kubernetes`](https://github.com/postech-software-architecture/workshop-infra-kubernetes).
Os valores dinâmicos precisam ser configurados na hora do deploy:

O Service `workshop-api` define explicitamente o Load Balancer como
`internet-facing`. Sem essa anotação, o AWS Load Balancer Controller adota o esquema
interno e o DNS resolve apenas para endereços privados da VPC.

> **Gate W3:** não execute este overlay até que o `db_client_sg_id` esteja anexado aos
> nodes do EKS ou o ADR-005 autorize o `node_security_group_id` no RDS. Sem isso, os
> pods não alcançam o PostgreSQL. Antes de qualquer `apply` do banco, importe a instância
> `workshop-db`, o subnet group e o security group existentes e confirme que o plan não
> propõe criar outra `aws_db_instance.postgres`.

## 1. Injetar endpoint e nome do RDS no ConfigMap

Obtenha `db_host` e `db_name` nos outputs do repositório
`workshop-infra-database`. Configure `DB_HOST` como secret e `DB_NAME` como variable
no GitHub Actions. A pipeline injeta ambos antes de aplicar o overlay. Em um deploy manual,
use `awk` para funcionar tanto com GNU/Linux quanto com macOS:

```bash
export DB_HOST='<endpoint-do-rds>'
export DB_NAME='workshop'
awk -v host="$DB_HOST" -v name="$DB_NAME" '
  /^DB_HOST=/ { print "DB_HOST=" host; next }
  /^DB_NAME=/ { print "DB_NAME=" name; next }
  { print }
' k8s/overlays/aws/config.env > k8s/overlays/aws/config.env.tmp
mv k8s/overlays/aws/config.env.tmp k8s/overlays/aws/config.env
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
