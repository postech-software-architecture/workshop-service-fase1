# `k8s/base` — recursos comuns (Kustomize)

Base do Kustomize com os recursos **iguais em todos os ambientes**. Os overlays (`dev`, `aws`)
referenciam esta base e adicionam/ajustam o que muda por ambiente (banco, exposição, segredos).

| Arquivo | Recurso | Notas |
|---|---|---|
| `namespace.yaml` | Namespace `workshop` | |
| `deployment.yaml` | Deployment `workshop-service` | 2 réplicas, probes (startup/readiness/liveness), requests/limits, `securityContext` non-root (UID 1000) |
| `service.yaml` | Service `ClusterIP` :8080 | acesso interno; exposição pública é no overlay `aws` |
| `hpa.yaml` | HorizontalPodAutoscaler | CPU 70%, **2→10 réplicas**, `scaleDown` estabilizado em 120s |
| `kustomization.yaml` | agrega os recursos acima | |

> O ConfigMap `workshop-config` e o Secret `workshop-secret` **não** ficam aqui — cada overlay
> gera o seu (é o que muda entre ambientes). Ver [`../README.md`](../README.md).

Pré-visualizar a base isolada: `kubectl kustomize k8s/base`.
