# Specification: Kubernetes Learning Journey

## Objective

Build practical, architectural competence in Kubernetes by designing, deploying, and operating a minimal set of interconnected microservices (a catalog API, an orders API, and a database) on a local `kind` cluster, before replicating it on GKE Autopilot and Amazon EKS.

The core lesson is understanding the **platform**: decoupled compute (Deployments), routing (Services), and state (StatefulSets). We will learn *when* to use each abstraction and what its boundaries are across local and cloud environments.

**System Architecture (Target Workloads):** 
- `catalog` (Spring Boot API)
- `postgres` (Stateful Database)
- `orders` (Spring Boot API)
- **Data Flow:** `orders` HTTP-GETs `catalog` → `catalog` queries `postgres`.

**Success:** You can deploy, configure, health-check, roll out, and rollback this system on `kind`, GKE Autopilot, and Amazon EKS, and you can explain the architectural boundaries of each component.

---

## The Architect's Lens & The Production Gap

As an architect, your focus is not just on `kubectl` commands, but on system design:

*   **Compute Boundaries:** Why Deployments are for stateless workloads and how ReplicaSets handle failure domains.
*   **Routing & Discovery:** How internal DNS (ClusterIP) isolates traffic, and when to expose endpoints externally (NodePort/Ingress).
*   **State Management:** Why StatefulSets are fundamentally different from Deployments, and how Persistent Volumes outlive the pods attached to them.
*   **Configuration:** How ConfigMaps and Secrets satisfy the 12-Factor App methodology by decoupling config from immutable images.

### The Production Gap
This repository is heavily optimized for **learning core K8s objects**. If taking this to **Production**, the following omitted pieces are mandatory:
*   **GitOps / CI/CD:** We use manual `kubectl apply`. Prod requires ArgoCD or Flux.
*   **Templating:** We use raw YAML and Kustomize. Prod often requires Helm.
*   **Security & RBAC:** We omit NetworkPolicies and granular RBAC.
*   **Managed Services:** We run Postgres in-cluster for learning state. Prod should use a managed DB (e.g., Cloud SQL or RDS).
*   **DNS & TLS:** We omit ExternalDNS and cert-manager.
*   **Observability:** We rely on `kubectl logs`. Prod requires Prometheus/Grafana and centralized logging.

---

## Tech Stack

| Piece | Choice | Architect's Justification |
|-------|--------|---------------------------|
| Local Cluster | `kind` | Provides a real Kubernetes API locally with zero cost. |
| CLI | `kubectl` | The universal interface for Kubernetes control planes. |
| Workload Apps | Java 25, Spring Boot 4 | Kept minimal (lightweight REST payloads) to focus strictly on K8s. |
| Database | Official `postgres` | Focus on K8s StatefulSets, not DB configuration. |
| Manifests | Raw YAML → Kustomize | Exposes the raw objects before introducing configuration management. |
| Cloud (GCP) | GKE Autopilot | Managed Kubernetes without node-pool management overhead. |
| Cloud (AWS) | Amazon EKS | Industry standard AWS managed Kubernetes with `eksctl`. |

### Prerequisites

Install the following tools before starting Playbook 00. Versions listed are the minimum tested.

| Tool | Version | Install |
|------|---------|---------|
| Docker Desktop | 4.x | [docker.com/get-started](https://www.docker.com/get-started/) |
| `kind` | 0.24+ | `brew install kind` or [kind.sigs.k8s.io](https://kind.sigs.k8s.io/docs/user/quick-start/#installation) |
| `kubectl` | 1.31+ | `brew install kubectl` or bundled with Docker Desktop |
| Java | 25 | [sdkman.io](https://sdkman.io/) (`sdk install java 25-open`) |
| Maven | 3.9+ | `brew install maven` or bundled with IDE |
| `gcloud` CLI | latest | [cloud.google.com/sdk](https://cloud.google.com/sdk/docs/install) — needed for Playbook 07 |
| `gke-gcloud-auth-plugin` | latest | `gcloud components install gke-gcloud-auth-plugin` — needed for GKE `kubectl` |

Verify your setup:

```bash
docker --version && kind --version && kubectl version --client && java -version && mvn -version
```

### Image Strategy

Images are built locally with `docker build` and loaded into the `kind` cluster via `kind load docker-image`. No remote registry is needed for local development.

```bash
# Example workflow (detailed in Playbook 00)
docker build -t shop/catalog:dev apps/catalog/
kind load docker-image shop/catalog:dev --name k8s-learn
```

For GKE Autopilot (Playbook 07), images are pushed to Google Artifact Registry instead.

---

## Step-by-Step Learning Playbooks

The learning journey is executed sequentially via **Playbooks** located in the `playbooks/` directory. Each playbook is an executable document to learn, implement, and verify a concept.

0.  **`00-containerize.md` (Build & Ship):** Build Docker images for `catalog` and `postgres`, load them into `kind`.
1.  **`01-basics.md` (Compute):** Pods, ReplicaSets, Deployments (Self-healing).
2.  **`02-networking.md` (Discovery & Routing):** ClusterIP, NodePort, Ingress (Traffic flow).
3.  **`03-state.md` (Storage):** StatefulSets, PV, PVC (Data survival).
4.  **`04-configuration.md` (Config):** ConfigMaps, Secrets (12-factor app config).
5.  **`05-communication.md` (Microservices):** Multi-app communication (`orders` → `catalog`).
6.  **`06-operations.md` (Resilience):** Liveness/Readiness Probes, Jobs, Rolling Updates (Zero-downtime).
7.  **`07-kustomize-gke.md` (Cloud & Overlays):** Kustomize base/overlays, GKE Autopilot deployment.

---

## Project Structure

> Directories are created incrementally as you progress through the playbooks.

```text
apps/catalog/          Spring Boot products API (Minimal payload)
apps/orders/           Spring Boot orders API (Minimal payload)
compose/               Docker Compose baseline
kind/                  kind cluster configuration
k8s/raw/               Plain YAML for early learning
k8s/base/              Kustomize base (Playbook 07)
k8s/overlays/          Kustomize overlays for kind and gke (Playbook 07)
playbooks/             Step-by-step executable guides
  ├── 00-containerize.md
  ├── 01-basics.md
  ├── 02-networking.md
  ├── 03-state.md
  ├── 04-configuration.md
  ├── 05-communication.md
  ├── 06-operations.md
  └── 07-kustomize-gke.md
docs/                  Specs, ADRs, and Concepts
```

---

## Code Style & Guidelines

*   **Minimalist Apps:** The Spring Boot applications will contain the absolute bare-minimum boilerplate required to run an HTTP server and connect to a database. No complex business logic, no authentication. They exist solely as lightweight test workloads to validate Kubernetes orchestration and networking behaviors.
*   **YAML Clarity:** One resource per file where it aids learning. Consistent `app` labels across deployments (`catalog`, `orders`, `postgres`).
*   **Boundaries:**
    *   *Always:* Verify behavior on the local cluster before moving to the next playbook.
    *   *Never:* Deploy `orders` before `postgres` and `catalog` are functional. Never commit real credentials to git.

---

## Success Criteria

*   [ ] You can articulate the architectural difference between a Deployment and a StatefulSet.
*   [ ] `catalog` runs on `kind` as a Deployment behind a Service.
*   [ ] Config and secrets are injected, not baked into the container image.
*   [ ] Probes are configured; you have forced and observed a readiness failure.
*   [ ] Postgres data demonstrably survives a pod deletion.
*   [ ] `orders` successfully queries `catalog` via internal K8s DNS (`http://catalog:8080`).
*   [ ] A zero-downtime rolling update is performed and verified.
*   [ ] A Job runs a database schema/seed task successfully.
*   [ ] The entire multi-service application is deployed to GKE Autopilot using Kustomize overlays.
