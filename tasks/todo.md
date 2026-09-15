# Task List: Kubernetes Learning Journey

Companion to `tasks/plan.md`. Spec: `docs/SPEC-kubernetes-learning.md`.

Build (once apps exist): `./mvnw clean package -DskipTests` (from each `apps/<name>/` directory)

---

## Legend

| Tag | Meaning |
|-----|---------|
| 🤖 **AGENT** | Agent writes all files; no terminal commands needed from you |
| 🤚 **MANUAL** | You run all the commands; agent has nothing to write |
| 🔀 **SPLIT** | Agent writes files; you run the verification and cluster commands |

For SPLIT tasks, acceptance criteria items prefixed with 🤚 are yours to execute.

---

## Phase 1: Foundation (Apps & Documentation)

---

## Task 1 — 🔀 SPLIT: Scaffold `apps/catalog` Spring Boot application

**Description:** Generate a minimal Spring Boot 4.x / Java 25 application with only the `spring-boot-starter-web` dependency. Add one `@RestController` returning a hardcoded JSON payload (`{"service": "catalog", "status": "ok"}`). No database, no business logic — this app exists solely as a lightweight workload for Kubernetes.

**Acceptance criteria:**
- [x] `apps/catalog/` contains a valid Maven project with `pom.xml`, Maven wrapper, and `src/main/java/` layout
- [x] `pom.xml` pins `java.version` to 25 and uses Spring Boot 4.x parent
- [x] `CatalogApplication.java` with `@SpringBootApplication` and a `main` method
- [x] `CatalogController.java` with `@RestController`, `@GetMapping("/")` returning `{"service": "catalog", "status": "ok"}`
- [x] `application.yml` sets `server.port: 8080`
- [x] 🤚 Build succeeds: `./mvnw clean package -DskipTests` from `apps/catalog/`
- [x] 🤚 Run locally: `./mvnw spring-boot:run` → `curl localhost:8080` returns the expected JSON

**🤚 What to Observe:**
Watch the Spring Boot startup banner in your terminal. Notice the embedded Tomcat server starting on port 8080. This exact same process will later run inside a Docker container, then inside a Kubernetes Pod — the app itself has zero awareness of Kubernetes. K8s treats it as an ordinary process.

**Dependencies:** None

**Files likely touched:**
- `apps/catalog/pom.xml`
- `apps/catalog/src/main/java/com/k8slearn/catalog/CatalogApplication.java`
- `apps/catalog/src/main/java/com/k8slearn/catalog/CatalogController.java`
- `apps/catalog/src/main/resources/application.yml`

**Estimated scope:** Small

---

## Task 2 — 🔀 SPLIT: Scaffold `apps/orders` Spring Boot application

**Description:** Same as Task 1 but for the `orders` service. Returns `{"service": "orders", "status": "ok"}`. Runs on port 8083 locally to avoid collisions with catalog during development.

**Acceptance criteria:**
- [x] `apps/orders/` contains a valid Maven project with `pom.xml`, Maven wrapper, and `src/main/java/` layout
- [x] `pom.xml` pins `java.version` to 25 and uses Spring Boot 4.x parent
- [x] `OrdersApplication.java` with `@SpringBootApplication` and a `main` method
- [x] `OrdersController.java` with `@RestController`, `@GetMapping("/")` returning `{"service": "orders", "status": "ok"}`
- [x] `application.yml` sets `server.port: 8083`
- [x] 🤚 Build succeeds: `./mvnw clean package -DskipTests` from `apps/orders/`
- [x] 🤚 Run locally: `./mvnw spring-boot:run` → `curl localhost:8083` returns the expected JSON

**🤚 What to Observe:**
Two completely independent services with no knowledge of each other — yet they will later be wired together purely through Kubernetes constructs (Services, DNS, environment variables). The application code will not change when the wiring is added.

**Dependencies:** None (parallel with Task 1)

**Files likely touched:**
- `apps/orders/pom.xml`
- `apps/orders/src/main/java/com/k8slearn/orders/OrdersApplication.java`
- `apps/orders/src/main/java/com/k8slearn/orders/OrdersController.java`
- `apps/orders/src/main/resources/application.yml`

**Estimated scope:** Small

---

## Task 3 — 🔀 SPLIT: Write Dockerfiles for both applications

**Description:** Create a multi-stage Dockerfile for each app: stage 1 builds the JAR with Maven, stage 2 runs it on `eclipse-temurin:25-jre`. This keeps images small and decouples the build environment from the runtime.

**Acceptance criteria:**
- [x] `apps/catalog/Dockerfile` uses multi-stage build (Maven build → JRE runtime)
- [x] `apps/orders/Dockerfile` uses multi-stage build
- [x] Both use `eclipse-temurin:25-jre` as the runtime base
- [x] `EXPOSE` matches the app's port (8080 for catalog, 8083 for orders)
- [x] `.dockerignore` in each app directory excludes `target/`, `.idea/`, `.git/`
- [x] 🤚 Build catalog image: `docker build -t shop/catalog:dev apps/catalog/`
- [x] 🤚 Build orders image: `docker build -t shop/orders:dev apps/orders/`
- [x] 🤚 Smoke test: `docker run --rm -p 8080:8080 shop/catalog:dev` → `curl localhost:8080` returns expected JSON

**🤚 What to Observe:**
Watch the multi-stage build layers print in sequence. Stage 1 (Maven) pulls the internet; Stage 2 (JRE) is tiny. After the build, run `docker images | grep shop` — note the image size. The multi-stage approach keeps the runtime image lean by excluding the Maven toolchain. You will need `imagePullPolicy: Never` in Kubernetes because these images are local-only and have never been pushed to a registry.

**Dependencies:** Task 1, Task 2

**Files likely touched:**
- `apps/catalog/Dockerfile`, `apps/catalog/.dockerignore`
- `apps/orders/Dockerfile`, `apps/orders/.dockerignore`

**Estimated scope:** Small

---

## Task 4a — 🤖 AGENT: Create root `README.md`

**Description:** Write the project-level README that orients a new reader. Explains what this project is, learning philosophy, quick start commands, manual steps reference, and links to the playbooks in order.

**Acceptance criteria:**
- [x] `README.md` at repo root with sections: What This Is, Learning Philosophy, Prerequisites, Quick Start, Playbook Index, Manual Steps at a Glance, What You Will Be Able to Explain, Project Structure
- [x] Prerequisites section matches the spec (Docker, kind, kubectl, Java 25, Maven, gcloud)
- [x] Playbook index lists all 8 playbooks (00-07) with the commands the user will run
- [x] Quick Start includes the verbatim `kind create cluster`, `docker build`, `kind load`, `kubectl apply` commands

**Verification:**
- [x] Links are valid; no broken references

**Dependencies:** None (parallel with Tasks 1-3)

**Files likely touched:**
- `README.md`

**Estimated scope:** XS

---

## Task 4b — 🤖 AGENT: Create `docs/CONCEPTS.md` learning glossary

**Description:** Create the living document where K8s concepts are recorded as they are learned. Pre-seeded with guiding prompts per concept to scaffold thinking — the learner fills in answers in their own words after each playbook.

**Acceptance criteria:**
- [x] `docs/CONCEPTS.md` with sections matching every concept in Playbooks 01–07
- [x] Each section has 3–5 guiding prompts (questions, not answers)
- [x] Each section has a `<!-- Your notes go here -->` placeholder
- [x] Final section is a capstone Deployment vs. StatefulSet comparison table

**Verification:**
- [x] File renders correctly in markdown; links to playbooks are valid

**Dependencies:** None (parallel with Tasks 1-3)

**Files likely touched:**
- `docs/CONCEPTS.md`

**Estimated scope:** XS

---

## Checkpoint: After Tasks 1-4

- [x] 🤚 `./mvnw clean package -DskipTests` succeeds in both `apps/catalog/` and `apps/orders/`
- [x] 🤚 `docker build` succeeds for both `shop/catalog:dev` and `shop/orders:dev`
- [x] `README.md`, `docs/CONCEPTS.md`, and `docs/DESIGN.md` exist and are consistent
- [x] Human review before any cluster work begins

---

## Phase 2: Local Cluster — Core Compute & Networking (Playbooks 00–02)

---

## Task 5 — 🤚 MANUAL: Execute Playbook 00 — create kind cluster, build and load images

**Description:** Follow `playbooks/00-containerize.md` to create the `k8s-learn` kind cluster, build both Docker images, and load them into the cluster's container runtime. Agent writes `kind/cluster-config.yaml`; you run everything.

**Acceptance criteria:**
- [x] 🤚 `kind create cluster --name k8s-learn --config kind/cluster-config.yaml` succeeds
- [x] 🤚 `kubectl cluster-info --context kind-k8s-learn` returns cluster info
- [x] 🤚 `kind load docker-image shop/catalog:dev --name k8s-learn` succeeds
- [x] 🤚 `kind load docker-image shop/orders:dev --name k8s-learn` succeeds
- [x] 🤚 Images visible inside the node: `docker exec -it k8s-learn-control-plane crictl images | grep shop`

**🤚 What to Observe:**
`kind create cluster` spins up a full Kubernetes control plane inside a Docker container on your machine. Run `docker ps` after — you will see a container named `k8s-learn-control-plane`. That container *is* your cluster.

When you run `kind load docker-image`, the image is copied from Docker's local storage into the cluster's container runtime (containerd), which is separate from Docker. This is why `imagePullPolicy: Never` is required — the image exists in containerd, not in a registry, and cannot be "pulled" in the normal sense. The `crictl images` command queries containerd directly to verify the image made it in.

**Dependencies:** Task 3

**Files likely touched (agent writes):**
- `kind/cluster-config.yaml`

**Estimated scope:** XS (config file; rest is command execution)

---

## Task 6 — 🔀 SPLIT: Create `k8s/raw/01-catalog-deployment.yaml`

**Description:** Agent writes the first Kubernetes manifest — a Deployment for the `catalog` app. You validate it against the cluster.

**Acceptance criteria:**
- [x] `k8s/raw/01-catalog-deployment.yaml` defines a `Deployment` named `catalog` in namespace `shop`
- [x] `replicas: 2`
- [x] Container image `shop/catalog:dev` with `imagePullPolicy: Never`
- [x] Container port 8080
- [x] Labels `app: catalog` on both the Deployment and the pod template
- [x] Resource requests/limits set (128Mi/256Mi memory, 250m/500m CPU)
- [x] 🤚 Client-side validation: `kubectl apply --dry-run=client -f k8s/raw/01-catalog-deployment.yaml`
- [x] 🤚 Server-side validation: `kubectl apply --dry-run=server -f k8s/raw/01-catalog-deployment.yaml -n shop`

**🤚 What to Observe:**
The server-side dry-run response includes a `metadata.resourceVersion` field and fully populated defaults (e.g., `terminationGracePeriodSeconds`, `dnsPolicy`). These are injected by Kubernetes admission controllers and default values — they are not in your YAML. This is the full object as Kubernetes sees it, not just what you wrote.

**Dependencies:** Task 5 (for server-side dry-run; cluster must be running)

**Files likely touched:**
- `k8s/raw/01-catalog-deployment.yaml`

**Estimated scope:** XS

---

## Task 7 — 🤚 MANUAL: Execute Playbook 01 — deploy catalog, prove self-healing

**Description:** Follow `playbooks/01-basics.md` to deploy the catalog Deployment and prove Kubernetes self-healing. You run all commands and fill in CONCEPTS.md afterward.

**Acceptance criteria:**
- [x] 🤚 `kubectl apply -f k8s/raw/01-catalog-deployment.yaml -n shop` succeeds
- [x] 🤚 `kubectl get pods -n shop -l app=catalog` shows 2/2 Ready
- [x] 🤚 Delete one pod: `kubectl delete pod <name> -n shop` → a replacement pod appears automatically
- [x] 🤚 Fill in Pods and ReplicaSets & Deployments sections in `docs/CONCEPTS.md`

**🤚 What to Observe:**
Watch the `-w` output after `kubectl apply`. Pods move through `Pending` → `ContainerCreating` → `Running`. After you delete a pod, watch how the ReplicaSet detects the mismatch (desired=2, actual=1) and immediately schedules a new pod — without any instruction from you. This is the declarative control loop: you declared intent, Kubernetes continuously enforces it.

**Dependencies:** Task 5, Task 6

**Files likely touched:**
- `docs/CONCEPTS.md` (fill in Playbook 01 concepts)

**Estimated scope:** XS (CONCEPTS fill-in; rest is command execution)

---

## Task 8 — 🔀 SPLIT: Write and execute Playbook 02 — Services (ClusterIP, NodePort)

**Description:** Agent authors `playbooks/02-networking.md` and the Service manifests. You apply them and verify traffic routing — both internal (from inside the cluster) and external (from your host machine).

**Acceptance criteria:**
- [x] `playbooks/02-networking.md` follows established playbook format (Architect's Concept, Implementation Steps, Execution, Verify & Prove with What to Observe, Teardown, After This Playbook)
- [x] `k8s/raw/02-catalog-service.yaml` — ClusterIP Service named `catalog` on port 8080
- [x] `k8s/raw/02-catalog-nodeport.yaml` — NodePort Service (nodePort: 30080)
- [x] 🤚 `kubectl apply -f k8s/raw/02-catalog-service.yaml -n shop` succeeds
- [x] 🤚 Internal: `kubectl exec -it <any-pod> -n shop -- curl http://catalog:8080` returns catalog JSON
- [x] 🤚 External: `curl localhost:30080` returns catalog JSON (requires kind `extraPortMappings`)
- [x] 🤚 Fill in Services & DNS section in `docs/CONCEPTS.md`

**🤚 What to Observe:**
When you `curl http://catalog:8080` from inside the cluster, Kubernetes DNS (kube-dns) resolved the name `catalog` to the Service's ClusterIP — a stable virtual IP that does not change even when pods restart. The Service then load-balances your request across the 2 catalog pods. This is why you use service names, not pod IPs.

**Dependencies:** Task 7

**Files likely touched:**
- `playbooks/02-networking.md`
- `k8s/raw/02-catalog-service.yaml`
- `k8s/raw/02-catalog-nodeport.yaml`
- `docs/CONCEPTS.md`

**Estimated scope:** Small

---

## Checkpoint: After Tasks 5-8

- [x] 🤚 `catalog` running as a Deployment with 2 replicas behind a ClusterIP Service on `kind`
- [x] 🤚 Self-healing proven (pod deletion → automatic recreation)
- [x] 🤚 Internal and external `curl` to catalog succeeds
- [x] `docs/CONCEPTS.md` Pods, ReplicaSets & Deployments, Services & DNS sections filled
- [x] Human review before state and configuration work

---

## Phase 3: Local Cluster — State, Config, Communication, Operations (Playbooks 03–06)

---

## Task 9 — 🔀 SPLIT: Write and execute Playbook 03 — postgres StatefulSet, PV/PVC

**Description:** Agent authors `playbooks/03-state.md` and the StatefulSet manifests. You apply them and prove data persistence by surviving a pod deletion.

**Acceptance criteria:**
- [x] `playbooks/03-state.md` follows established format
- [x] `k8s/raw/03-postgres-statefulset.yaml` — StatefulSet named `postgres`, 1 replica, `postgres:16-alpine`, `volumeClaimTemplates` requesting 1Gi
- [x] `k8s/raw/03-postgres-service.yaml` — headless Service for the StatefulSet
- [x] 🤚 `kubectl apply -f` both manifests in the `shop` namespace
- [x] 🤚 `kubectl exec -it postgres-0 -n shop -- psql -U postgres` → insert a test row → exit
- [x] 🤚 `kubectl delete pod postgres-0 -n shop` → wait for pod to restart as `postgres-0`
- [x] 🤚 `kubectl exec -it postgres-0 -n shop -- psql -U postgres` → verify the row still exists
- [x] 🤚 Fill in StatefulSets & Persistent Volumes section in `docs/CONCEPTS.md`

**🤚 What to Observe:**
After deletion, the new pod comes back as `postgres-0` — not a random name. The StatefulSet guarantees stable, ordered identity. The data survived because the PersistentVolumeClaim (PVC) was not deleted — it is a separate object that outlives the pod. The pod is ephemeral; the volume is not. Run `kubectl get pvc -n shop` to see the claim still bound after the pod restart.

**Dependencies:** Task 7

**Files likely touched:**
- `playbooks/03-state.md`
- `k8s/raw/03-postgres-statefulset.yaml`
- `k8s/raw/03-postgres-service.yaml`
- `docs/CONCEPTS.md`

**Estimated scope:** Small

---

## Task 10 — 🔀 SPLIT: Write and execute Playbook 04 — ConfigMaps, Secrets, wire catalog → postgres

**Description:** Agent authors `playbooks/04-configuration.md`, ConfigMap/Secret manifests, and updates the catalog app to connect to postgres. You apply and verify the database connection.

**Acceptance criteria:**
- [x] `playbooks/04-configuration.md` follows established format
- [x] `k8s/raw/04-catalog-configmap.yaml` contains `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/catalog`
- [x] `k8s/raw/04-catalog-secret.yaml` contains base64-encoded DB username and password
- [x] `k8s/raw/01-catalog-deployment.yaml` updated with `envFrom` referencing ConfigMap and Secret
- [x] `catalog` app updated: `spring-boot-starter-data-jpa` + `postgresql` driver in `pom.xml`; `Product` entity; `GET /products` endpoint
- [x] 🤚 `kubectl apply -f` ConfigMap, Secret, and updated Deployment
- [x] 🤚 `kubectl describe pod <catalog-pod> -n shop` — inspect the Environment section
- [x] 🤚 `kubectl logs <catalog-pod> -n shop` — verify successful postgres connection
- [x] 🤚 `curl http://catalog:8080/products` (from inside cluster) returns data or empty list
- [x] 🤚 Fill in ConfigMaps & Secrets section in `docs/CONCEPTS.md`

**🤚 What to Observe:**
In `kubectl describe pod`, look at the Environment section. You will see entries like `SPRING_DATASOURCE_URL: <set to the key ... from ConfigMap>` and `SPRING_DATASOURCE_PASSWORD: <set to the key ... from Secret>`. Kubernetes does not expose Secret values in plain text in pod descriptions. The application code did not change to accommodate this — only the K8s config did. This is 12-Factor App config in action.

**Dependencies:** Task 9

**Files likely touched:**
- `playbooks/04-configuration.md`
- `k8s/raw/04-catalog-configmap.yaml`
- `k8s/raw/04-catalog-secret.yaml`
- `k8s/raw/01-catalog-deployment.yaml` (add envFrom)
- `apps/catalog/pom.xml` (add JPA + postgres)
- `apps/catalog/src/main/java/com/k8slearn/catalog/Product.java`
- `apps/catalog/src/main/java/com/k8slearn/catalog/ProductRepository.java`
- `apps/catalog/src/main/java/com/k8slearn/catalog/CatalogController.java` (add /products)
- `docs/CONCEPTS.md`

**Estimated scope:** Medium

---

## Task 11 — 🔀 SPLIT: Write and execute Playbook 05 — deploy orders, internal DNS communication

**Description:** Agent authors `playbooks/05-communication.md` and orders manifests. You deploy orders and prove the inter-service call works via Kubernetes DNS.

**Acceptance criteria:**
- [x] `playbooks/05-communication.md` follows established format
- [x] `k8s/raw/05-orders-deployment.yaml` — orders Deployment with `CATALOG_URL=http://catalog:8080`
- [x] `k8s/raw/05-orders-service.yaml` — ClusterIP Service for orders
- [x] `orders` app updated: `GET /orders` endpoint that HTTP-GETs catalog's `/products` and combines the response
- [x] 🤚 `kubectl apply -f` orders Deployment and Service
- [x] 🤚 `kubectl exec -it <orders-pod> -n shop -- curl http://catalog:8080/products` — succeeds
- [x] 🤚 `curl http://orders:8083/orders` (from inside cluster) — returns combined data
- [x] 🤚 Fill in Internal DNS & Cross-Service Communication section in `docs/CONCEPTS.md`

**🤚 What to Observe:**
When orders calls `http://catalog:8080`, it is using the *Service name* as a DNS hostname. Kubernetes DNS resolves `catalog` → ClusterIP → one of the catalog pods. If catalog pods are rescheduled and get new IPs, the DNS entry and ClusterIP stay stable — orders never needs to know. This is how microservices achieve location transparency in Kubernetes.

**Dependencies:** Task 10

**Files likely touched:**
- `playbooks/05-communication.md`
- `k8s/raw/05-orders-deployment.yaml`
- `k8s/raw/05-orders-service.yaml`
- `apps/orders/src/main/java/com/k8slearn/orders/OrdersController.java`
- `apps/orders/pom.xml` (if RestClient dependency needed)
- `docs/CONCEPTS.md`

**Estimated scope:** Medium

---

## Task 12 — 🔀 SPLIT: Write and execute Playbook 06 — Probes, Jobs, Rolling Updates

**Description:** Agent authors `playbooks/06-operations.md`, probe configuration, and the seed Job. You force a readiness failure, run the Job, and perform a rolling update with rollback.

**Acceptance criteria:**
- [x] `playbooks/06-operations.md` follows established format
- [x] Catalog Deployment updated with `livenessProbe` and `readinessProbe` (HTTP GET `/actuator/health`)
- [x] `catalog` app updated: `spring-boot-starter-actuator` added to `pom.xml`
- [x] `k8s/raw/06-seed-job.yaml` — Job running `psql` to insert seed product data
- [x] 🤚 `kubectl apply -f` updated Deployment and Job
- [x] 🤚 Force readiness failure → `kubectl get pods -n shop` shows `0/1 Ready` for catalog
- [x] 🤚 `kubectl rollout status deployment/catalog -n shop` — verify clean state
- [x] 🤚 Update image tag → `kubectl set image deployment/catalog catalog=shop/catalog:v2 -n shop`
- [x] 🤚 Watch rolling update: `kubectl rollout status -w deployment/catalog -n shop`
- [x] 🤚 `kubectl rollout undo deployment/catalog -n shop` — rollback and verify
- [x] 🤚 Fill in Probes, Jobs, and Rolling Updates sections in `docs/CONCEPTS.md`

**🤚 What to Observe:**
When you force the readiness failure, the pod stays in `Running` state (liveness is OK) but transitions to `0/1 Ready`. Kubernetes removes it from the Service's endpoint list — traffic stops reaching it, but the pod is not killed. This is the readiness/liveness distinction: readiness gates traffic, liveness gates pod restarts.

During the rolling update, watch `kubectl get pods -w`. Old pods scale down one at a time while new pods come up. The Service routes only to `Ready` pods throughout — zero downtime is maintained by the readiness probe, not by magic.

**Dependencies:** Task 11

**Files likely touched:**
- `playbooks/06-operations.md`
- `k8s/raw/01-catalog-deployment.yaml` (add probes)
- `k8s/raw/06-seed-job.yaml`
- `apps/catalog/pom.xml` (add actuator)
- `docs/CONCEPTS.md`

**Estimated scope:** Medium

---

## Checkpoint: After Tasks 9-12

- [x] Postgres data survives pod deletion
- [x] Config and secrets are injected, not baked into images
- [x] `orders` queries `catalog` via `http://catalog:8080`
- [x] Zero-downtime rolling update performed and verified
- [x] Probes configured; readiness failure forced and observed
- [x] Job successfully seeded the database
- [x] `docs/CONCEPTS.md` Playbooks 03–06 sections filled
- [ ] Human review before GKE transition

---

## Phase 4: Cloud Transition — Kustomize & GKE Autopilot (Playbook 07)

---

## Task 13 — 🤖 AGENT: Write Playbook 07 (GKE)

**Description:** Author `playbooks/07-kustomize-gke.md`. Covers: why Kustomize (single base, environment-specific overlays), full GCP project bootstrap from scratch, GKE Autopilot cluster provisioning, Artifact Registry setup, image push, overlay deployment, and a mandatory cleanup section.

**Acceptance criteria:**
- [x] `playbooks/07-kustomize-gke.md` follows established format
- [x] Covers why Kustomize, base vs. overlays, and the learning payoff (only 2 manifest lines differ between clouds)
- [x] Includes full `gcloud` bootstrap sequence: `gcloud projects create` → billing link → API enablement → Artifact Registry → GKE Autopilot cluster
- [x] Includes `$5/month` GCP budget alert setup instructions (Billing → Budgets)
- [x] Includes a diagram showing the kind vs. GKE overlay diff (what changed: image path + StorageClass)
- [x] Includes mandatory **Cleanup** section: `gcloud projects delete <project-id>` with explanation of 30-day grace period

**Dependencies:** Task 12

**Files likely touched:**
- `playbooks/07-kustomize-gke.md`

**Estimated scope:** Small

---

## Task 14 — 🔀 SPLIT: Copy `k8s/raw/` into `k8s/base/` with `kustomization.yaml`

**Description:** Agent creates the Kustomize base by copying finalized raw manifests and adding `kustomization.yaml`. `k8s/raw/` remains untouched. You validate the Kustomize output matches the raw originals.

**Acceptance criteria:**
- [x] `k8s/base/` contains copies of all deployment, service, statefulset, configmap, and secret manifests
- [x] `k8s/base/kustomization.yaml` lists all resources
- [x] `k8s/raw/` is unchanged
- [x] 🤚 `kubectl kustomize k8s/base/` renders valid YAML
- [x] 🤚 Diff: output matches raw originals (ordering may differ, content should not)

**🤚 What to Observe:**
`kubectl kustomize k8s/base/` should output YAML identical (modulo ordering) to concatenating all your raw manifests. This confirms that the base is a faithful copy — no changes yet. The overlay is where environment-specific differences live.

**Dependencies:** Task 12 (all raw manifests must be finalized)

**Files likely touched:**
- `k8s/base/` (all manifests + `kustomization.yaml`)

**Estimated scope:** Small

---

## Task 15 — 🔀 SPLIT: Create kind, GKE, and EKS overlays

**Description:** Agent creates environment-specific overlays that patch the base for each target environment. Three overlays are created: `kind/` (local), `gke/` (GKE Autopilot), and `eks/` (Amazon EKS stub — fully wired in Task 18 with real ECR values).

**Acceptance criteria:**
- [x] `k8s/overlays/kind/kustomization.yaml` references `../../base`; patches: `imagePullPolicy: Never`, local image names, `standard` StorageClass
- [x] `k8s/overlays/gke/kustomization.yaml` references `../../base`; patches: Artifact Registry image path, `standard-rwo` StorageClass, higher resource limits
- [x] `k8s/overlays/eks/kustomization.yaml` references `../../base`; stub patches: placeholder ECR image path (`<account>.dkr.ecr.<region>.amazonaws.com/shop/<app>:dev`), `gp3` StorageClass
- [x] 🤚 `kubectl kustomize k8s/overlays/kind/` renders valid YAML
- [x] 🤚 `kubectl kustomize k8s/overlays/gke/` renders valid YAML
- [x] 🤚 `kubectl kustomize k8s/overlays/eks/` renders valid YAML (stub placeholder values are fine at this stage)
- [x] 🤚 Diff the kind vs. GKE outputs — only image path + StorageClass lines should differ
- [x] 🤚 Diff the GKE vs. EKS outputs — only image registry URL + StorageClass name should differ (same pattern, different values)

**🤚 What to Observe:**
Run: `diff <(kubectl kustomize k8s/overlays/gke/) <(kubectl kustomize k8s/overlays/eks/)`. The diff will show exactly two types of changes: image registry URLs and StorageClass names. Everything else — Deployments, Services, ConfigMaps, Secrets, probes, the seed Job — is byte-for-byte identical. This is the core value of Kustomize: one source of truth, clearly named exceptions.

**Dependencies:** Task 14

**Files likely touched:**
- `k8s/overlays/kind/kustomization.yaml` and patches
- `k8s/overlays/gke/kustomization.yaml` and patches
- `k8s/overlays/eks/kustomization.yaml` and stub patches

**Estimated scope:** Small

---

## Task 16 — 🤚 MANUAL: Execute Playbook 07 — create GCP project, push to Artifact Registry, deploy to GKE Autopilot

**Description:** Follow `playbooks/07-kustomize-gke.md` to create a dedicated GCP project from scratch, authenticate, push images to Artifact Registry, provision a GKE Autopilot cluster, and deploy using the GKE overlay. Run the cleanup at the end.

**Acceptance criteria:**
- [x] 🤚 New GCP project created: `gcloud projects create k8s-learn-<yourname>`
- [x] 🤚 Billing account linked and required APIs enabled
- [x] 🤚 $5/month budget alert configured in GCP Billing console
- [x] 🤚 Artifact Registry repository created and `docker` configured to push to it
- [x] 🤚 Images pushed: `docker push <region>-docker.pkg.dev/<project>/shop/catalog:dev` and `orders:dev`
- [x] 🤚 GKE Autopilot cluster created and `kubectl` context switched
- [x] 🤚 `kubectl apply -k k8s/overlays/gke/` deploys all resources to GKE
- [x] 🤚 `kubectl get pods -n shop` on GKE context shows all pods Running
- [x] 🤚 `orders → catalog → postgres` data flow verified end-to-end on GKE
- [x] 🤚 Fill in Kustomize section in `docs/CONCEPTS.md`
- [ ] 🤚 **Cleanup:** `gcloud projects delete <project-id>` run and confirmed

**🤚 What to Observe:**
`kubectl apply -k k8s/overlays/gke/` works identically to the kind cluster — the same command, the same manifests, a completely different infrastructure. GKE Autopilot provisions nodes automatically based on your Pod resource requests (you never specified a node count). Run `kubectl get nodes` — you will see nodes that appeared automatically. The Kubernetes abstraction held across environments.

After cleanup: `gcloud projects delete` enters a 30-day pending deletion window. Billing stops immediately. You can run `gcloud projects list` to confirm the project status is `DELETE_REQUESTED`.

**Dependencies:** Task 13, Task 15, GCP account access

**Files likely touched:**
- `docs/CONCEPTS.md`

**Estimated scope:** XS (concepts fill-in; rest is command execution)

---

## Checkpoint: GKE Complete

- [ ] 🤚 Full system running on GKE Autopilot via `k8s/overlays/gke/`
- [ ] 🤚 `orders → catalog → postgres` end-to-end verified on GKE
- [ ] `docs/CONCEPTS.md` Kustomize section filled
- [ ] 🤚 GCP project deleted (`gcloud projects delete` confirmed, status `DELETE_REQUESTED`)
- [ ] Human review before EKS phase

---

## Phase 5: Cloud Transition — Amazon EKS (Playbook 08)

---

## Task 17 — 🤖 AGENT: Write Playbook 08 (EKS)

**Description:** Author `playbooks/08-kustomize-eks.md`. Covers AWS CLI setup, ECR repository creation, EKS cluster provisioning via `eksctl`, EBS CSI add-on enablement (required for `gp3` StorageClass), image push, overlay deployment, and a mandatory cleanup section.

**Acceptance criteria:**
- [ ] `playbooks/08-kustomize-eks.md` follows established format
- [ ] Covers AWS CLI + `eksctl` installation and authentication (`aws configure`)
- [ ] Includes ECR repository creation commands for `catalog` and `orders`
- [ ] Includes `eksctl create cluster` command with EBS CSI add-on flag
- [ ] Explains why EBS CSI add-on is needed for `gp3` PVCs (without it, PVCs stay `Pending`)
- [ ] Includes `$10/month` AWS Budget alert setup instructions
- [ ] Includes side-by-side comparison: what changed from GKE overlay to EKS overlay (image URL + StorageClass only)
- [ ] Includes mandatory **Cleanup** section: `eksctl delete cluster`, `aws ecr delete-repository` for each repo, `aws ec2 describe-volumes` orphan EBS volume check

**Dependencies:** Task 16 (GKE complete)

**Files likely touched:**
- `playbooks/08-kustomize-eks.md`

**Estimated scope:** Small

---

## Task 18 — 🔀 SPLIT: Finalize `k8s/overlays/eks/`

**Description:** Agent updates the EKS overlay stub (created in Task 15) with real ECR image path format and confirmed `gp3` StorageClass. You verify the rendered output is valid and diff it against the GKE overlay.

**Acceptance criteria:**
- [ ] `k8s/overlays/eks/kustomization.yaml` uses `gp3` StorageClass patch
- [ ] `k8s/overlays/eks/` image patches reference real ECR URL format: `<account>.dkr.ecr.<region>.amazonaws.com/shop/<app>:dev`
- [ ] 🤚 `kubectl kustomize k8s/overlays/eks/` renders valid YAML
- [ ] 🤚 `diff <(kubectl kustomize k8s/overlays/gke/) <(kubectl kustomize k8s/overlays/eks/)` shows only image URL + StorageClass name differences

**🤚 What to Observe:**
The diff between GKE and EKS overlays is the entire list of cloud-specific differences for this application. Two lines differ. Everything else — the application logic, health probes, service definitions, the seed job, replica counts — is identical. The Kubernetes portability promise is demonstrated concretely.

**Dependencies:** Task 17

**Files likely touched:**
- `k8s/overlays/eks/kustomization.yaml` and patches

**Estimated scope:** XS

---

## Task 19 — 🤚 MANUAL: Execute Playbook 08 — push to ECR, provision EKS cluster, deploy, cleanup

**Description:** Follow `playbooks/08-kustomize-eks.md` to configure AWS credentials, create ECR repositories, push images, provision an EKS cluster with `eksctl`, deploy using the EKS overlay, verify end-to-end, and run mandatory cleanup.

**Acceptance criteria:**
- [ ] 🤚 $10/month AWS Budget alert configured before starting
- [ ] 🤚 AWS CLI configured (`aws configure`) and `eksctl` installed
- [ ] 🤚 ECR repositories created for `catalog` and `orders`
- [ ] 🤚 Images pushed: `docker push <account>.dkr.ecr.<region>.amazonaws.com/shop/catalog:dev` and `orders:dev`
- [ ] 🤚 EKS cluster provisioned: `eksctl create cluster --name k8s-learn --region <region>`
- [ ] 🤚 EBS CSI add-on enabled (required for `gp3` PVC provisioning)
- [ ] 🤚 `kubectl apply -k k8s/overlays/eks/` deploys all resources to EKS
- [ ] 🤚 `kubectl get pods -n shop` on EKS context shows all pods Running
- [ ] 🤚 `orders → catalog → postgres` data flow verified end-to-end on EKS
- [ ] 🤚 Fill in capstone comparison in `docs/CONCEPTS.md`: kind vs. GKE vs. EKS — what differed, what was identical
- [ ] 🤚 **Cleanup:** `eksctl delete cluster --name k8s-learn`, `aws ecr delete-repository` for both repos, verify no orphaned EBS volumes with `aws ec2 describe-volumes --filters Name=status,Values=available`

**🤚 What to Observe:**
`kubectl apply -k k8s/overlays/eks/` is the same command as on kind and GKE. The application has no idea it changed clouds. Run `kubectl get nodes` — you will see EC2 instances (unlike GKE Autopilot's virtual nodes). This is the difference between serverless Kubernetes (Autopilot) and managed node groups (EKS): same API, different infrastructure model underneath.

During cleanup, always run the `aws ec2 describe-volumes` check for stranded EBS volumes. PVCs that are deleted before the cluster shuts down can leave orphaned EBS volumes that continue incurring storage charges.

**Dependencies:** Task 17, Task 18, AWS account access

**Files likely touched:**
- `docs/CONCEPTS.md`
- `README.md`

**Estimated scope:** XS (concepts fill-in; rest is command execution)

---

## Checkpoint: Complete

- [ ] `k8s/raw/` intact alongside `k8s/base/` and `k8s/overlays/` (kind, gke, eks)
- [ ] Full system verified on kind, GKE Autopilot, and Amazon EKS
- [ ] `docs/CONCEPTS.md` fully populated: all playbooks + capstone kind vs. GKE vs. EKS comparison
- [ ] 🤚 GCP project deleted (charges stopped, status `DELETE_REQUESTED`)
- [ ] 🤚 EKS cluster deleted, ECR repos deleted, no orphaned EBS volumes
- [x] `README.md` reflects multi-cloud support
- [ ] Human review

