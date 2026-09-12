# Kubernetes Learning Journey

> **Learn by doing, understand by observing.**  
> A structured, hands-on journey from zero to deploying a production-like multi-service system on Kubernetes — locally with `kind`, then on GKE Autopilot.

---

## What This Is

A learning project, not a production system. We build three interconnected services (`catalog`, `orders`, `postgres`) and deliberately touch every major Kubernetes abstraction along the way. The goal is not to ship software — it is to deeply understand the platform.

See [`docs/DESIGN.md`](docs/DESIGN.md) for the full system architecture and design decisions.  
See [`docs/SPEC-kubernetes-learning.md`](docs/SPEC-kubernetes-learning.md) for requirements and tech stack.

---

## Learning Philosophy

> 🤚 **You run the cluster. The agent writes the code.**

This repo splits responsibility deliberately:

| Who | Does What |
|-----|-----------|
| 🤖 Agent | Scaffolds apps, writes Dockerfiles, authors YAML manifests, writes playbooks and docs |
| 🤚 You | Runs every `docker`, `kubectl`, `kind`, and `gcloud` command |

**Why?** Because the learning happens in the execution. You need to see the pod state transitions, watch DNS resolve a service name, and force a readiness failure yourself. A command that works is interesting; understanding *why* it worked is the goal.

Every manual step in the playbooks includes a **"🤚 What to Observe"** section. Do not skip it — that is where the architectural insight lives.

After each playbook, fill in your learnings in [`docs/CONCEPTS.md`](docs/CONCEPTS.md) in your own words.

---

## Prerequisites

Install these tools before starting Playbook 00:

| Tool | Min Version | Install |
|------|------------|---------|
| Docker Desktop | 4.x | [docker.com/get-started](https://www.docker.com/get-started/) |
| `kind` | 0.24+ | `brew install kind` |
| `kubectl` | 1.31+ | `brew install kubectl` or bundled with Docker Desktop |
| Java | 25 LTS | `sdk install java 25-open` via [sdkman.io](https://sdkman.io/) |
| Maven | 3.9+ | `brew install maven` |
| `gcloud` CLI | latest | [cloud.google.com/sdk](https://cloud.google.com/sdk/docs/install) — Playbook 07 only |

Verify your setup:

```bash
docker --version && kind --version && kubectl version --client && java -version && mvn -version
```

---

## Quick Start

> These commands assume Phase 1 (app scaffolding, Dockerfiles) is complete.  
> Work through the tasks in [`tasks/todo.md`](tasks/todo.md) in order — Phase 1 first.

```bash
# 1. Create the kind cluster
kind create cluster --name k8s-learn --config kind/cluster-config.yaml
kubectl cluster-info --context kind-k8s-learn   # verify you are connected

# 2. Build the application images
docker build -t shop/catalog:dev apps/catalog/
docker build -t shop/orders:dev apps/orders/

# 3. Load images into the kind cluster (required — kind uses its own container runtime)
kind load docker-image shop/catalog:dev --name k8s-learn
kind load docker-image shop/orders:dev --name k8s-learn

# 4. Create the namespace and deploy catalog
kubectl create namespace shop
kubectl apply -f k8s/raw/01-catalog-deployment.yaml -n shop

# 5. Verify
kubectl get pods -n shop -l app=catalog
```

For the full walkthrough with context, concepts, and what to observe at each step, start with [Playbook 00](playbooks/00-containerize.md).

---

## Playbook Index

| # | Playbook | Core Concept | 🤚 Commands You Will Run |
|---|---------|-------------|--------------------------|
| 00 | [00-containerize.md](playbooks/00-containerize.md) | Docker → kind | `docker build`, `kind create cluster`, `kind load docker-image` |
| 01 | [01-basics.md](playbooks/01-basics.md) | Pods, ReplicaSets, Deployments | `kubectl apply`, `kubectl get pods -w`, `kubectl delete pod` |
| 02 | [02-networking.md](playbooks/02-networking.md) | Services, ClusterIP, NodePort | `kubectl apply`, `kubectl exec` → `curl`, `curl localhost:30080` |
| 03 | [03-state.md](playbooks/03-state.md) | StatefulSets, PV/PVC | `kubectl exec` → `psql`, delete pod, verify data survives |
| 04 | [04-configuration.md](playbooks/04-configuration.md) | ConfigMaps, Secrets | `kubectl describe pod`, `kubectl logs`, `curl /products` |
| 05 | [05-communication.md](playbooks/05-communication.md) | Internal DNS, cross-service | `kubectl exec` → `curl http://catalog:8080`, `curl /orders` |
| 06 | [06-operations.md](playbooks/06-operations.md) | Probes, Jobs, Rolling Updates | Force NotReady, `kubectl rollout status`, `kubectl rollout undo` |
| 07 | [07-kustomize-gke.md](playbooks/07-kustomize-gke.md) | Kustomize, GKE Autopilot | `gcloud`, `docker push`, `kubectl apply -k` |

---

## Manual Steps at a Glance

Quick reference for every 🤚 command across all playbooks:

| Playbook | Commands |
|----------|----------|
| 00 | `docker build`, `kind create cluster --config`, `kind load docker-image`, `docker exec crictl images` |
| 01 | `kubectl create namespace`, `kubectl apply -f`, `kubectl get pods -w`, `kubectl delete pod` |
| 02 | `kubectl apply -f` (Service), `kubectl exec` → `curl http://catalog:8080`, `curl localhost:30080` |
| 03 | `kubectl apply -f` (StatefulSet), `kubectl exec` → `psql` → insert row, `kubectl delete pod`, verify row |
| 04 | `kubectl apply -f` (ConfigMap, Secret), `kubectl describe pod`, `kubectl logs <pod>`, `curl /products` |
| 05 | `kubectl apply -f` (orders), `kubectl exec` → `curl http://catalog:8080`, `curl /orders` |
| 06 | `kubectl apply -f` (probes, job), force readiness fail, `kubectl rollout status`, `kubectl rollout undo` |
| 07 | `gcloud auth login`, `docker push`, `kubectl apply -k overlays/gke/`, `kubectl get pods` (GKE context) |

---

## What You Will Be Able to Explain

When this journey is complete, you can articulate:

- [ ] The architectural difference between a `Deployment` and a `StatefulSet`
- [ ] Why StatefulSet pods have stable names (`postgres-0`) while Deployment pods have random suffixes
- [ ] How Kubernetes DNS resolves `http://catalog:8080` without hardcoding an IP address
- [ ] Why `ConfigMaps` and `Secrets` satisfy the 12-Factor App methodology
- [ ] What a `readinessProbe` does vs. a `livenessProbe`, and when each fires
- [ ] How Kustomize base/overlays eliminate config duplication between environments
- [ ] Why GKE Autopilot does not require you to manage node pools

These map directly to the [success criteria](docs/SPEC-kubernetes-learning.md#success-criteria) in the spec.

---

## Project Structure

```
apps/catalog/          Spring Boot catalog API (minimal payload → postgres)
apps/orders/           Spring Boot orders API (calls catalog)
kind/                  kind cluster configuration
k8s/raw/               Plain YAML manifests (Playbooks 01–06, never deleted)
k8s/base/              Kustomize base (Playbook 07)
k8s/overlays/kind/     kind-specific Kustomize overlay
k8s/overlays/gke/      GKE Autopilot Kustomize overlay
playbooks/             Step-by-step executable guides (00–07)
docs/
  ├── SPEC-kubernetes-learning.md   Requirements & tech stack
  ├── DESIGN.md                     System architecture & key decisions
  └── CONCEPTS.md                   Your living learning glossary (fill as you go)
tasks/
  ├── plan.md           Implementation plan & architecture decisions
  └── todo.md           Task list with AGENT / MANUAL / SPLIT breakdown
```
