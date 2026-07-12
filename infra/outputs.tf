output "cluster_name" {
  description = "Nome do cluster (contexto kubectl: kind-<name>)"
  value       = kind_cluster.this.name
}

output "kubeconfig_path" {
  description = "Caminho do kubeconfig gerado (Dev 2 usa como secret KUBECONFIG_B64 do CD)"
  value       = kind_cluster.this.kubeconfig_path
}

output "db_host" {
  description = "Host do Postgres dentro do cluster (Dev 3 -> DB_HOST)"
  value       = "postgresql.${var.namespace}.svc.cluster.local"
}

output "db_port" {
  description = "Porta do Postgres (Dev 3 -> DB_PORT)"
  value       = 5432
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
  description = "Senha do banco (Dev 3 -> DB_PASSWORD, vai para Secret)"
  value       = var.db_password
  sensitive   = true
}

output "db_jdbc_url" {
  description = "JDBC URL de conveniencia"
  value       = "jdbc:postgresql://postgresql.${var.namespace}.svc.cluster.local:5432/${var.db_name}"
}
