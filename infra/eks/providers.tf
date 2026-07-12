provider "aws" {
  region = var.region
}

# Autenticacao no cluster EKS via `exec` (aws eks get-token) em vez de um token estatico
# do data source: o token do data source e resolvido no plan e tem TTL de ~15 min, mas a
# criacao do cluster leva ~15 min — ele expiraria antes do helm do metrics-server rodar,
# quebrando o apply. O exec gera o token na hora, herdando o profile/credenciais do lab.
locals {
  eks_exec = {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name, "--region", var.region]
  }
}

provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

  exec {
    api_version = local.eks_exec.api_version
    command     = local.eks_exec.command
    args        = local.eks_exec.args
  }
}

provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

    exec {
      api_version = local.eks_exec.api_version
      command     = local.eks_exec.command
      args        = local.eks_exec.args
    }
  }
}
