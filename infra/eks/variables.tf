variable "region" {
  description = "Regiao AWS"
  type        = string
  default     = "us-east-1"
}

variable "project" {
  description = "Prefixo de nomeacao dos recursos"
  type        = string
  default     = "workshop"
}

variable "db_name" {
  description = "Nome do banco"
  type        = string
  # Alinhado ao contrato do repo e ao overlay k8s/overlays/aws (DB_NAME=mydatabase).
  default     = "mydatabase"
}

variable "db_username" {
  description = "Usuario do banco"
  type        = string
  default     = "workshop"
}

variable "db_password" {
  description = "Senha do banco. Em producao use aws_secretsmanager_secret."
  type        = string
  default     = "workshop"
  sensitive   = true
}
