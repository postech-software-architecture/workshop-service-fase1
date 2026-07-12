# Trilha B — EKS + RDS (evolução, NÃO aplicar na entrega)

Esqueleto de infraestrutura de nuvem (AWS) como caminho de evolução da Trilha A. **Não roda no
CI e não deve receber `terraform apply` na entrega** — serve para demonstrar domínio de IaC em
nuvem e expõe os **mesmos contratos** (`db_host/port/name/username/password`, `cluster_name`) que
a Trilha A, para o Dev 3 consumir sem alteração.

## Recursos

- `module.vpc` — VPC com subnets públicas/privadas e 1 NAT Gateway (single, para custo).
- `module.eks` — cluster EKS 1.30 com node group gerenciado (t3.medium, 1–3 nós).
- `aws_db_instance.postgres` — RDS Postgres 15 (db.t3.micro), acessível só pelos nós do EKS (SG).

## Custo e credenciais

- Requer credenciais AWS (`aws configure` ou `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`).
- **Gera custo:** control plane EKS (~US$0,10/h), 2× EC2 t3.medium, RDS db.t3.micro, NAT Gateway
  (~US$0,045/h) + tráfego. **Sempre `terraform destroy` após demonstrar.**
- A senha do banco aqui é `var.db_password` para simplicidade; em produção use
  `aws_secretsmanager_secret` e injete a credencial fora do state.

## Validação (só estática — sem apply)

```bash
cd infra/eks
terraform init -backend=false
terraform validate
```

## kubeconfig (quando aplicado)

```bash
aws eks update-kubeconfig --name workshop-eks --region us-east-1
```
