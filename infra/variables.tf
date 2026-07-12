variable "cluster_name" {
  description = "Nome do cluster kind"
  type        = string
  default     = "workshop"
}

variable "kubeconfig_path" {
  description = "Caminho onde o kubeconfig do cluster sera escrito"
  type        = string
  default     = "./kubeconfig"
}

variable "namespace" {
  description = "Namespace onde o Postgres sera instalado"
  type        = string
  default     = "workshop"
}

variable "db_name" {
  description = "Nome do banco de dados da aplicacao"
  type        = string
  # Alinhado ao contrato do repo (compose.yaml, application*.yaml, k8s ConfigMap): mydatabase.
  default     = "mydatabase"
}

variable "db_username" {
  description = "Usuario do banco"
  type        = string
  default     = "workshop"
}

variable "db_password" {
  description = "Senha do banco (para dev/entrega). Em cloud, use secret manager."
  type        = string
  default     = "workshop"
  sensitive   = true
}

variable "postgres_chart_version" {
  description = "Versao do chart bitnami/postgresql"
  type        = string
  default     = "16.2.1"
}
