# Trilha B — EKS + RDS adaptada ao AWS Academy Learner Lab (entrega em nuvem).
# O Academy bloqueia criacao de IAM (so a LabRole existente e usavel), entao NAO criamos
# roles: reusamos a LabRole no cluster e nos nodes, e mapeamos a role do lab como admin.
# Aplicar com credenciais do Academy (AWS Details -> inclui aws_session_token, expira ~4h).
# Ver infra/eks/README.md. Sempre `terraform destroy` ao final.

data "aws_availability_zones" "available" {
  state = "available"
}

# LabRole pre-existente do Academy — unica role usavel (nao ha permissao para criar IAM).
data "aws_iam_role" "lab" {
  name = "LabRole"
}

# Identidade do caller (a role do lab, ex.: voclabs) — mapeada como admin do cluster.
data "aws_caller_identity" "current" {}

# --- Rede ---
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.13"

  name = "${var.project}-vpc"
  cidr = "10.0.0.0/16"

  azs             = slice(data.aws_availability_zones.available.names, 0, 2)
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true # reduz custo (NAT Gateway e caro)

  tags = { Project = var.project }
}

# --- Cluster EKS ---
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.24"

  cluster_name    = "${var.project}-eks"
  cluster_version = "1.30"

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  cluster_endpoint_public_access = true

  # Academy: nao criar IAM role do cluster — reusar a LabRole.
  create_iam_role = false
  iam_role_arn    = data.aws_iam_role.lab.arn

  # Academy Learner Lab bloqueia IAM e KMS: desabilita tudo que exigiria criar um
  # OIDC provider (IRSA), uma KMS key (encriptacao de secrets) ou um log group —
  # cada um daria AccessDenied e faria o apply falhar no meio.
  enable_irsa                 = false
  create_kms_key              = false
  cluster_encryption_config   = {}
  create_cloudwatch_log_group = false
  cluster_enabled_log_types   = []

  # Acesso ao cluster: mapeia AUTOMATICAMENTE quem cria o cluster (a role "voclabs"
  # das credenciais do lab) como admin — sem isso o kubectl fica Unauthorized. O
  # access_entry explicito da LabRole abaixo cobre a LabRole operar o cluster tambem.
  authentication_mode                      = "API_AND_CONFIG_MAP"
  enable_cluster_creator_admin_permissions = true

  access_entries = {
    lab = {
      principal_arn = data.aws_iam_role.lab.arn
      policy_associations = {
        admin = {
          policy_arn = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
          access_scope = {
            type = "cluster"
          }
        }
      }
    }
  }

  eks_managed_node_groups = {
    default = {
      instance_types = ["t3.medium"]
      min_size       = 1
      max_size       = 3
      desired_size   = 2

      # Academy: nodes tambem reusam a LabRole (sem criar IAM role do node group).
      create_iam_role = false
      iam_role_arn    = data.aws_iam_role.lab.arn
    }
  }

  tags = { Project = var.project }
}

# --- metrics-server (necessario para o HPA do Dev 3 ler CPU) ---
# Instalado via Helm apos o cluster existir; sem ele o autoscaling nao mede nada.
resource "helm_release" "metrics_server" {
  name       = "metrics-server"
  namespace  = "kube-system"
  repository = "https://kubernetes-sigs.github.io/metrics-server/"
  chart      = "metrics-server"

  set {
    name  = "args[0]"
    value = "--kubelet-insecure-tls"
  }

  depends_on = [module.eks]
}

# --- Banco RDS Postgres ---
resource "aws_db_subnet_group" "this" {
  name       = "${var.project}-db-subnets"
  subnet_ids = module.vpc.private_subnets
}

resource "aws_security_group" "db" {
  name   = "${var.project}-db-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [module.eks.node_security_group_id] # so os nodes acessam
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Project = var.project }
}

resource "aws_db_instance" "postgres" {
  identifier             = "${var.project}-db"
  engine                 = "postgres"
  engine_version         = "15"
  instance_class         = "db.t3.micro"
  allocated_storage      = 20
  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password # em prod: aws_secretsmanager_secret
  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.db.id]
  skip_final_snapshot    = true
  publicly_accessible    = false
}
