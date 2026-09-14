# Implementation Plan: Kubernetes Learning Journey

## Overview

Build practical Kubernetes competence by designing, deploying, and operating three interconnected microservices (`catalog`, `orders`, `postgres`) on a local `kind` cluster, then replicating the deployment on **GKE Autopilot** and **Amazon EKS** using Kustomize overlays. Requirements live in `docs/SPEC-kubernetes-learning.md`. Playbooks in `playbooks/` are the executable learning surface. Task details and checkpoints live in `tasks/todo.md`.

## Architecture Decisions

- **Spring Boot 4 / Java 25 LTS.** As specified in `SPEC-kubernetes-learning.md`, we use Java 25 LTS and Spring Boot 4.x for modern enterprise microservices.
- **Minimal apps, maximal K8s.** Each Spring Boot app has exactly one `@RestController` returning a hardcoded JSON payload (no database logic in `catalog` until Playbook 04). The apps exist solely to validate Kubernetes behaviors.
- **Image strategy is `docker build` → `kind load`.** No remote registry until Playbook 07 (GKE). Images are tagged `shop/<app>:dev`. The `kind` cluster is named `k8s-learn`.
- **`k8s/raw/` is permanent.** When Kustomize is introduced in Playbook 07, the raw YAML is **copied** into `k8s/base/`, not moved or deleted. Both directories coexist so the learner can diff raw vs. Kustomize approaches.
- **Playbooks are written incrementally.** Each playbook is authored immediately before its execution, not all upfront. Playbook 01 already exists; the rest are created as Phase 2/3/4/5 tasks.
- **`docs/CONCEPTS.md` is a living glossary.** After each playbook, the concepts learned are recorded here with the learner's own mental model, not just definitions.
- **Namespace is `shop`.** All workloads deploy to a single namespace for simplicity. Namespace isolation is noted in CONCEPTS.md but not exercised across multiple namespaces.
- **Dedicated GCP project.** A brand-new GCP project is created solely for this learning exercise. `gcloud projects delete <id>` at the end wipes everything cleanly with zero billing risk to other workloads.
- **EKS StorageClass is `gp3`.** Newer, cheaper, better performance than `gp2`. Requires EBS CSI driver add-on enabled on the cluster.
- **Shared Kustomize base.** `k8s/base/` is identical across all cloud targets. Only the overlay differs — and between GKE and EKS, exactly two things change: image registry URL and StorageClass name.

### Dependency graph

```
Spring Boot apps (Task 1-2)
    │
    ├── Dockerfiles (Task 3)
    │       │
    │       └── Playbook 00: kind cluster + image load (Task 5)
    │               │
    │               └── K8s raw manifests (Task 6)
    │                       │
    │                       └── Playbook 01: Compute (Task 7)
    │                               │
    │                               ├── Playbook 02: Networking (Task 8)
    │                               │       │
    │                               │       └── Playbook 03: State (Task 9)
    │                               │               │
    │                               │               └── Playbook 04: Configuration (Task 10)
    │                               │                       │
    │                               │                       └── Playbook 05: Communication (Task 11)
    │                               │                               │
    │                               │                               └── Playbook 06: Operations (Task 12)
    │                               │
    │                               └── (all playbooks feed Kustomize)
    │
    ├── Playbook 07: Kustomize + GKE (Tasks 13-16)
    │       │
    │       └── Playbook 08: EKS (Tasks 17-19)  ← reuses k8s/base/ and overlay pattern

Documentation (Tasks 4a, 4b) — parallel with all phases
```

### Build / Run commands

- **Build app:** `./mvnw clean package -DskipTests` (from each `apps/<name>/` directory)
- **Build image:** `docker build -t shop/<name>:dev apps/<name>/`
- **Load into kind:** `kind load docker-image shop/<name>:dev --name k8s-learn`
- **Apply manifest:** `kubectl apply -f k8s/raw/<file>.yaml -n shop`
- **Verify pods:** `kubectl get pods -n shop`

## Task List

Index only. Full acceptance criteria, verification, dependencies, and files are in `tasks/todo.md`.

### Phase 1: Foundation (Apps & Documentation)

- [x] Task 1: Scaffold `apps/catalog` Spring Boot application
- [x] Task 2: Scaffold `apps/orders` Spring Boot application
- [x] Task 3: Write Dockerfiles for both applications
- [x] Task 4a: Create root `README.md`
- [x] Task 4b: Create `docs/CONCEPTS.md` learning glossary

### Checkpoint: After Tasks 1-4

- [x] `./mvnw clean package -DskipTests` succeeds in both app directories
- [x] `docker build` succeeds for both images
- [x] README and CONCEPTS files exist and are internally consistent with the spec
- [x] Review with human before Playbook execution

### Phase 2: Local Cluster — Core Compute & Networking (Playbooks 00-02)

- [x] Task 5: Execute Playbook 00 — create `kind` cluster, build and load images
- [x] Task 6: Create `k8s/raw/01-catalog-deployment.yaml`
- [x] Task 7: Execute Playbook 01 — deploy catalog, prove self-healing
- [x] Task 8: Write and execute Playbook 02 — Services (ClusterIP, NodePort)

### Checkpoint: After Tasks 5-8

- [x] `catalog` running as a Deployment with 2 replicas behind a ClusterIP Service on `kind`
- [x] Self-healing proven (pod deletion → automatic recreation)
- [x] Internal and external `curl` to catalog succeeds
- [x] `docs/CONCEPTS.md` Pods, ReplicaSets & Deployments, Services & DNS sections filled
- [x] Review with human before state and configuration work

### Phase 3: Local Cluster — State, Config, Communication, Operations (Playbooks 03-06)

- [x] Task 9: Write and execute Playbook 03 — postgres StatefulSet, PV/PVC
- [x] Task 10: Write and execute Playbook 04 — ConfigMaps, Secrets, wire catalog → postgres
- [x] Task 11: Write and execute Playbook 05 — deploy orders, internal DNS communication
- [x] Task 12: Write and execute Playbook 06 — Probes, Jobs, Rolling Updates

### Checkpoint: After Tasks 9-12

- [x] Postgres data survives pod deletion
- [x] Config and secrets are injected, not baked into images
- [x] `orders` queries `catalog` via `http://catalog:8080`
- [x] Zero-downtime rolling update performed and verified
- [x] Probes configured; readiness failure forced and observed
- [x] Job successfully seeded the database
- [x] `docs/CONCEPTS.md` Playbooks 03–06 sections filled
- [ ] Review with human before GKE transition

### Phase 4: Cloud Transition — Kustomize & GKE Autopilot (Playbook 07)

- [x] Task 13: Write Playbook 07 — full GCP project bootstrap, budget alert, Kustomize motivation
- [ ] Task 14: Copy `k8s/raw/` into `k8s/base/` with `kustomization.yaml`
- [ ] Task 15: Create `k8s/overlays/kind/`, `k8s/overlays/gke/`, and stub `k8s/overlays/eks/`
- [ ] Task 16: Execute Playbook 07 — create GCP project, push to Artifact Registry, deploy to GKE Autopilot

### Checkpoint: GKE Complete

- [ ] Full system running on GKE Autopilot via `k8s/overlays/gke/`
- [ ] `orders → catalog → postgres` end-to-end verified on GKE
- [ ] `docs/CONCEPTS.md` Kustomize section filled
- [ ] GCP project deleted (charges stopped)
- [ ] Review with human before EKS phase

### Phase 5: Cloud Transition — Amazon EKS (Playbook 08)

- [ ] Task 17: Write Playbook 08 — AWS setup, `eksctl` cluster, ECR, EBS CSI add-on, budget alert, cleanup
- [ ] Task 18: Finalize `k8s/overlays/eks/` — real ECR image paths, `gp3` StorageClass
- [ ] Task 19: Execute Playbook 08 — push to ECR, provision EKS cluster, deploy via EKS overlay, cleanup

### Checkpoint: Complete

- [ ] Full system running on Amazon EKS via `k8s/overlays/eks/`
- [ ] `orders → catalog → postgres` end-to-end verified on EKS
- [ ] Capstone comparison in `docs/CONCEPTS.md`: kind vs. GKE vs. EKS — what differed, what was identical
- [ ] EKS cluster deleted, ECR repositories deleted (charges stopped)
- [ ] `k8s/raw/` intact alongside `k8s/base/` and all three overlays
- [ ] `README.md` reflects multi-cloud support
- [ ] Review with human

## Parallelization Opportunities

- **Safe to parallelize:** Task 4a ∥ Task 4b ∥ Tasks 1-3; Task 1 ∥ Task 2
- **Must be sequential:** Task 3 after 1+2; Task 5 after 3; each playbook task after the previous; Task 14 after all raw manifests exist
- **Needs coordination:** Playbook 05 (orders → catalog) requires both apps to have correct service names and ports

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Toolchain / dependency compatibility | Low | Java 25 LTS installed locally; Spring Boot 4 starter configured and verified |
| `kind load docker-image` fails silently | Med | Verify with `crictl images` inside the kind node after every load |
| Unexpected GCP charges | Low | Dedicated project + `gcloud projects delete` at end + $5 budget alert before starting |
| Unexpected AWS charges | Med | `eksctl delete cluster` + ECR delete documented as mandatory cleanup. $10 budget alert before execution |
| EBS CSI driver not enabled on EKS | Med | Playbook 08 includes `eksctl` add-on commands; without it `gp3` PVCs will stay `Pending` |
| EKS EC2 nodes idle after session | Med | Playbook 08 cleanup section is the mandatory last step; `eksctl delete cluster` terminates all nodes |
| Postgres StatefulSet on kind needs a StorageClass | Med | Use `standard` (kind default); document the difference vs. GKE `standard-rwo` and EKS `gp3` |
| Playbook ordering creates implicit coupling | Low | Each playbook has an explicit prerequisites section referencing prior playbooks |

## Open Questions

All resolved:
1. **GCP project** — Dedicated new project created from scratch. Playbook 07 includes full bootstrap (`gcloud projects create` → billing → APIs → Artifact Registry → cluster).
2. **EKS execution** — Real deployment. `eksctl` + ECR. Actual workloads run, then cleaned up.
3. **EKS StorageClass** — `gp3` (EBS CSI driver).
