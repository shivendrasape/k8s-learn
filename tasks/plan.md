# Implementation Plan: Kubernetes Learning Journey

## Overview

Build practical Kubernetes competence by designing, deploying, and operating three interconnected microservices (`catalog`, `orders`, `postgres`) on a local `kind` cluster, then replicating the deployment on GKE Autopilot using Kustomize overlays. Requirements live in `docs/SPEC-kubernetes-learning.md`. Playbooks in `playbooks/` are the executable learning surface. Task details and checkpoints live in `tasks/todo.md`.

## Architecture Decisions

- **Spring Boot 4 / Java 25 LTS.** As specified in `SPEC-kubernetes-learning.md`, we use Java 25 LTS and Spring Boot 4.x for modern enterprise microservices.
- **Minimal apps, maximal K8s.** Each Spring Boot app has exactly one `@RestController` returning a hardcoded JSON payload (no database logic in `catalog` until Playbook 04). The apps exist solely to validate Kubernetes behaviors.
- **Image strategy is `docker build` → `kind load`.** No remote registry until Playbook 07 (GKE). Images are tagged `shop/<app>:dev`. The `kind` cluster is named `k8s-learn`.
- **`k8s/raw/` is permanent.** When Kustomize is introduced in Playbook 07, the raw YAML is **copied** into `k8s/base/`, not moved or deleted. Both directories coexist so the learner can diff raw vs. Kustomize approaches.
- **Playbooks are written incrementally.** Each playbook is authored immediately before its execution, not all upfront. Playbook 01 already exists; the rest are created as Phase 2/3/4 tasks.
- **`docs/CONCEPTS.md` is a living glossary.** After each playbook, the concepts learned are recorded here with the learner's own mental model, not just definitions.
- **Namespace is `shop`.** All workloads deploy to a single namespace for simplicity. Namespace isolation is noted in CONCEPTS.md but not exercised across multiple namespaces.

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
    └── Playbook 07: Kustomize + GKE (Task 13-16)

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
- [ ] Task 10: Write and execute Playbook 04 — ConfigMaps, Secrets, wire catalog → postgres
- [ ] Task 11: Write and execute Playbook 05 — deploy orders, internal DNS communication
- [ ] Task 12: Write and execute Playbook 06 — Probes, Jobs, Rolling Updates

### Checkpoint: After Tasks 9-12

- [ ] Postgres data survives pod deletion
- [ ] Config and secrets are injected, not baked into images
- [ ] `orders` queries `catalog` via `http://catalog:8080`
- [ ] Zero-downtime rolling update performed and verified
- [ ] Probes configured; readiness failure forced and observed
- [ ] Job successfully seeded the database
- [ ] `docs/CONCEPTS.md` Playbooks 03–06 sections filled
- [ ] Review with human before GKE transition

### Phase 4: Cloud Transition — Kustomize & GKE Autopilot (Playbook 07)

- [ ] Task 13: Write Playbook 07
- [ ] Task 14: Copy `k8s/raw/` into `k8s/base/` with `kustomization.yaml`
- [ ] Task 15: Create `k8s/overlays/kind/` and `k8s/overlays/gke/` overlays
- [ ] Task 16: Execute Playbook 07 — push images to Artifact Registry, deploy to GKE Autopilot

### Checkpoint: Complete

- [ ] All spec success criteria met (see `docs/SPEC-kubernetes-learning.md` Success Criteria)
- [ ] `k8s/raw/` intact alongside `k8s/base/` and `k8s/overlays/`
- [ ] Full system running on GKE Autopilot via Kustomize overlays
- [ ] `docs/CONCEPTS.md` fully populated across all playbooks, including the capstone comparison
- [ ] `README.md` reflects the final project state
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
| GKE Autopilot quota / billing not set up | Med | Task 13 includes `gcloud` setup commands; ask user about GCP project first |
| Postgres StatefulSet on kind needs a StorageClass | Med | Use `standard` (kind default); document the difference vs. GKE's `standard-rwo` |
| Playbook ordering creates implicit coupling | Low | Each playbook has an explicit prerequisites section referencing prior playbooks |

## Open Questions

- For the GKE transition in Phase 4, do you already have a GCP project set up, or should Playbook 07 include `gcloud` commands to create a project, enable billing, and provision the Autopilot cluster from scratch?
- Should `catalog` connect to postgres from Playbook 03 (State) or Playbook 04 (Configuration)? The spec implies configuration comes after state. Current plan: Playbook 03 deploys postgres standalone, Playbook 04 wires catalog to it via ConfigMap/Secret.
