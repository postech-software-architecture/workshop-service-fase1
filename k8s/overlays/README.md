# `k8s/overlays` — ambientes (Kustomize)

Overlays que compõem a [`../base`](../base/README.md) para cada ambiente. O que muda entre eles é
o **banco** (in-cluster vs. RDS), a **exposição** (port-forward vs. LoadBalancer) e a origem dos
**segredos**.

| Overlay | Ambiente | Banco | Exposição | Segredos |
|---|---|---|---|---|
| [`dev/`](dev/README.md) | Local (kind/minikube) | Postgres **in-cluster** (`postgres.yaml`) | `port-forward` | `secret.env` (valores DUMMY, versionados) |
| [`aws/`](aws/README.md) | EKS + RDS (AWS Academy) | **RDS** (endpoint injetado no deploy) | `Service type=LoadBalancer` → ELB | criados no deploy (CD/Terraform), **não** versionados |

```bash
kubectl apply -k k8s/overlays/dev     # local
kubectl apply -k k8s/overlays/aws     # nuvem (EKS)
kubectl kustomize k8s/overlays/aws    # só renderiza, não aplica
```
