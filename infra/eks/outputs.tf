# Mesmos contratos da Trilha A, para o Dev 3 consumir sem mudar nada.
output "cluster_name" {
  description = "Nome do cluster EKS"
  value       = module.eks.cluster_name
}

output "db_host" {
  description = "Endpoint do RDS (Dev 3 -> DB_HOST)"
  value       = aws_db_instance.postgres.address
}

output "db_port" {
  description = "Porta do RDS (Dev 3 -> DB_PORT)"
  value       = aws_db_instance.postgres.port
}

output "db_name" {
  description = "Nome do banco (Dev 3 -> DB_NAME)"
  value       = var.db_name
}

output "db_username" {
  description = "Usuario do banco (Dev 3 -> DB_USER)"
  value       = var.db_username
}

output "db_password" {
  description = "Senha do banco (Dev 3 -> DB_PASSWORD)"
  value       = var.db_password
  sensitive   = true
}

# kubeconfig no EKS: `aws eks update-kubeconfig --name <cluster_name> --region <region>`
