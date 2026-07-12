resource "kind_cluster" "this" {
  name            = var.cluster_name
  kubeconfig_path = pathexpand(var.kubeconfig_path)
  wait_for_ready  = true

  kind_config {
    kind        = "Cluster"
    api_version = "kind.x-k8s.io/v1alpha4"

    node {
      role = "control-plane"

      # Expoe a porta 30080 do node para o host (usada por NodePort/Ingress do Dev 3).
      extra_port_mappings {
        container_port = 30080
        host_port      = 30080
      }
    }

    node {
      role = "worker"
    }
  }
}

resource "kubernetes_namespace" "workshop" {
  metadata {
    name = var.namespace
  }

  depends_on = [kind_cluster.this]
}

resource "helm_release" "postgresql" {
  name       = "postgresql"
  namespace  = kubernetes_namespace.workshop.metadata[0].name
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "postgresql"
  version    = var.postgres_chart_version

  # A Bitnami moveu as imagens publicas de docker.io/bitnami/* para o catalogo legacy
  # (docker.io/bitnamilegacy/*) em 2025; o chart aponta para uma tag que nao existe mais no
  # repositorio antigo. Apontamos a imagem para o registro legacy (gratuito) para o pull funcionar.
  set {
    name  = "image.repository"
    value = "bitnamilegacy/postgresql"
  }

  # Autenticacao — alinhada aos outputs consumidos pelo Dev 3.
  set {
    name  = "auth.username"
    value = var.db_username
  }
  set_sensitive {
    name  = "auth.password"
    value = var.db_password
  }
  set {
    name  = "auth.database"
    value = var.db_name
  }

  # Ambiente de entrega: standalone, 1 replica, persistencia minima.
  set {
    name  = "architecture"
    value = "standalone"
  }
  set {
    name  = "primary.persistence.size"
    value = "1Gi"
  }

  depends_on = [kubernetes_namespace.workshop]
}
