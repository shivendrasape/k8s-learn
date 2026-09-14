# CONCEPTS.md — Kubernetes Learning Glossary

> **How to use this file:**  
> Fill in each section *after* completing the corresponding playbook — in your own words, not copied from docs.  
> The guiding prompts are questions to think through, not a template to fill in mechanically.  
> The goal: build a mental model you can explain to someone who has never used Kubernetes.

---

## Containers & Images

> 🤚 Fill this in after **Playbook 00** → [00-containerize.md](../playbooks/00-containerize.md)

**Think through:**
- What is the difference between a container image and a running container?
- What is the Docker "build context", and why does a multi-stage build not need the host's `target/` directory?
- What is the difference between `target/` on your host Mac vs `/workspace/target/` inside the builder container?
- What does an image tag like `shop/catalog:dev` mean, and why should you avoid `:latest` for images used in local Kubernetes clusters?
- Why can't a local `kind` cluster pull images from your host Docker daemon without `kind load docker-image`?

<!-- Your notes go here -->

---

## Cluster Architecture: Control Plane, Nodes, & `kubectl`

> 🤚 Fill this in after **Playbook 00** → [00-containerize.md](../playbooks/00-containerize.md)

**Think through:**
- What is the difference between the **Control Plane** and a **Worker Node** in a Kubernetes cluster?
- What are the four core components of the Control Plane (`kube-apiserver`, `etcd`, `kube-scheduler`, `kube-controller-manager`), and what does each do?
- How does `kind` simulate an entire Kubernetes node using a single Docker container (`k8s-learn-control-plane`)?
- What is `containerd`? Is it something you had to install on your Mac, or where does it live?
- Does the `kind` container run our `shop` images directly? How does container nesting work here?
- What does `docker exec -it k8s-learn-control-plane crictl images | grep shop` actually do, and why are there two separate image stores on your machine?
- Is `kubectl` specific to `kind`, or universal? How does `kubectl` know how to talk to your local cluster via `~/.kube/config`?
- Why did we need `kind/cluster-config.yaml` with `extraPortMappings` to expose port 30080 to our Mac host?

#### Reference: Local (`kind`) vs. Standard Cloud vs. Serverless Cloud

| Aspect | Local (`kind`) | Standard Cloud (GKE / EKS) | Serverless Cloud (GKE Autopilot / AWS Fargate) |
|---|---|---|---|
| **Control Plane** | Runs in `k8s-learn-control-plane` Docker container on your Mac. | Managed across redundant VMs by Google/AWS. Invisible and managed for you. | Managed across redundant VMs by Google/AWS. Invisible and managed for you. |
| **Worker Nodes** | Simulated inside the same Docker container. | Real Linux VMs (Google Compute Engine or AWS EC2). You choose machine types and manage node pools. | **No node management at all.** Google/AWS dynamically spins up compute on-demand for each Pod. |
| **Container Engine** | `containerd` inside the Docker container. | `containerd` installed natively on the Linux VMs. | `containerd` managed entirely by the cloud provider. |
| **Pricing Model** | Free (uses your Mac's RAM and CPU). | You pay for the underlying VMs 24/7, whether your pods use all CPU or not. | **Pay-per-Pod:** You only pay for the exact CPU and memory requested by running Pods. |
| **External Access** | NodePort + `extraPortMappings` to localhost. | Cloud Load Balancers or NodePort on public VM IPs. | Cloud Load Balancer provisioned automatically by K8s Service annotations. |

<!-- Your notes go here -->

---

## Pods

> 🤚 Fill this in after **Playbook 01** → [01-basics.md](../playbooks/01-basics.md)

**Think through:**
- What is a Pod, and what does it actually contain at runtime?
- Why does Kubernetes use Pods instead of running containers directly?
- Why does `curl localhost:8080` fail when Pods are running on a cluster node? What is the difference between a Pod's overlay IP and your Mac's host ports?
- How does `kubectl port-forward` bridge the gap between your Mac and a Pod?
- What happens to a Pod when the node it is running on dies?
- When would you ever put *two* containers in the same Pod?

<!-- Your notes go here -->

---

## ReplicaSets & Deployments

> 🤚 Fill this in after **Playbook 01** → [01-basics.md](../playbooks/01-basics.md)

**Think through:**
- What is the difference between a **Client-Side Dry Run** (`--dry-run=client`) and a **Server-Side Dry Run** (`--dry-run=server`)? What does the API server inject during server-side validation?
- What is a **Namespace** (like `shop`), and how does it organize workloads in both local clusters and Cloud consoles (GCP/AWS)?
- What problem does a ReplicaSet solve that a bare Pod cannot?
- If you delete a Pod that is managed by a Deployment, what happens and why?
- What is "desired state" vs "actual state"? Why does that distinction matter in operations?
- Why do you almost never create a ReplicaSet directly?

<!-- Your notes go here -->

---

## Services & DNS

> 🤚 Fill this in after **Playbook 02** → [02-networking.md](../playbooks/02-networking.md)

**Think through:**
- Why is `kubectl port-forward` strictly a temporary developer debugging tunnel, and why can it never be used for production traffic?
- What problem does a ClusterIP Service solve? Why can you not just use a Pod's IP directly?
- How does `curl http://catalog:8080` resolve inside the cluster? Who resolves `catalog`?
- What is the difference between a ClusterIP Service, a NodePort Service, and a Cloud LoadBalancer?
- What is the relationship between a Service's `selector` field and pod labels?

#### Reference: Reaching Pods (Debugging vs. Production)
- **`kubectl port-forward` (Developer Tunnel):** Connects a temporary pipe from your laptop to one specific Pod or Deployment through the Kubernetes API server. If you close your terminal or your laptop sleeps, the tunnel dies.
- **`Kubernetes Service` (Production Routing):** A durable, in-cluster load balancer. It assigns a stable virtual IP (`ClusterIP`) and DNS name. Even if Pods are killed and replaced with new IPs, the Service automatically routes traffic to healthy pods matching its label selector.

#### Reference: Service Types & External Access Comparison

| Service Type | Scope | External URL / IP? | Cloud Cost | Primary Use Case |
|---|---|---|---|---|
| **`ClusterIP`** *(Default)* | In-cluster only | ❌ None | Free | Internal microservices, databases, backend APIs (secure by default). |
| **`NodePort`** | Node network | Port on node IP (`<NodeIP>:30000-32767`) | Free | Local dev clusters (`kind`), bare-metal on-prem, direct internal routing. |
| **`LoadBalancer`** | Public Internet | ✅ Dedicated Cloud Public IPv4 | Cloud fee (~$18–$25/mo) | Exposing a single standalone service directly to the internet. |
| **`Ingress` / `Gateway API`** | Public Internet | ✅ Shared Public IPv4 / Domain | Single LB fee (~$18–$25/mo) | Production standard. Routes multiple microservices by URL path (`/products`, `/orders`) with TLS/SSL. |

<!-- Your notes go here -->

---

## StatefulSets & Persistent Volumes

> 🤚 Fill this in after **Playbook 03** → [03-state.md](../playbooks/03-state.md)

**Think through:**
- What are the three key differences between a Deployment and a StatefulSet?
- Why do StatefulSet pods have stable names (`postgres-0`) while Deployment pods have random suffixes?
- When the postgres pod is deleted, why does the data survive?
- What is the relationship between a PersistentVolumeClaim, a PersistentVolume, and a StorageClass?
- When would you use a StatefulSet for a workload that is *not* a database?

#### Reference: The Three Pillars of Stateful Workloads

| Mechanism | Component | Purpose |
|---|---|---|
| **Identity** | Headless Service (`clusterIP: None`) | Provides deterministic DNS records (`<pod>.<service>.<ns>.svc.cluster.local`) bypassing virtual proxy IPs. |
| **Storage** | `volumeClaimTemplates` + StorageClass | Dynamically provisions independent PVs per replica that persist beyond pod termination. |
| **Ordering** | StatefulSet Controller | Guarantees ordered startup (`0, 1, 2...`) and ordered termination (`...2, 1, 0`) to prevent split-brain states. |

> **Critical Safety Rule:** Deleting a StatefulSet will terminate its Pods, but will **never delete its PVCs**. Persistent Volume Claims must be deleted explicitly to avoid accidental data loss.

<!-- Your notes go here -->

---

## ConfigMaps & Secrets

> 🤚 Fill this in after **Playbook 04** → [04-configuration.md](../playbooks/04-configuration.md)

**Think through:**
- What is the 12-Factor App methodology, and how do ConfigMaps and Secrets implement it?
- What is the practical difference between injecting config via `envFrom` vs. mounting as a file?
- Why should you never bake environment-specific config into your container image?
- Kubernetes Secrets are base64-encoded, not encrypted. What does that mean for your security posture?
- How would you update a ConfigMap value without rebuilding the image?

#### Reference: Externalized Configuration in Kubernetes

| Primitive | Classification | Storage Form | Injection Methods | Update Behavior |
|---|---|---|---|---|
| **ConfigMap** | Non-sensitive app config (URLs, ports, profiles, flags) | Plain text in etcd | `envFrom`, `valueFrom`, Volume Mount (`/etc/config`) | Env vars require rollout restart; Volume mounts auto-update |
| **Secret** | Sensitive credentials (passwords, tokens, TLS keys) | Base64 encoded in etcd | `envFrom`, `secretKeyRef`, Volume Mount (`/etc/secrets`) | Masked in `describe pod`; requires etcd KMS encryption for true security at rest |

> **12-Factor Principle (Factor III):** Configuration must be strictly decoupled from the code. The container image remains immutable across all environments (dev, test, prod); only the ConfigMap and Secret definitions change per deployment target.
>
> **Spring Boot Precedence Hierarchy:**
> 1. `Container CLI Args` (`args: ["--prop=val"]`) — *Highest precedence*
> 2. `OS Environment Variables` (`envFrom` / `env` via ConfigMaps & Secrets)
> 3. `Application Properties` (`application.yml` via `${VAR:default}` placeholders) — *Lowest precedence*
>
> **Canonical Relaxed Binding:** Canonical environment variables like `SPRING_DATASOURCE_URL` bind directly to `spring.datasource.url` without requiring explicit `${...}` placeholders in `application.yml`. Custom properties can use `${VAR:default}` syntax for fallbacks.

<!-- Your notes go here -->

---

## Internal DNS & Cross-Service Communication

> 🤚 Fill this in after **Playbook 05** → [05-communication.md](../playbooks/05-communication.md)

**Think through:**
- How does `orders` locate `catalog` at runtime without knowing its IP address?
- What is the full DNS name for the `catalog` Service inside the `shop` namespace?
- If the catalog pod restarts and gets a new IP, why does `http://catalog:8080` still work?
- What would you need to change if you moved `catalog` to a different namespace?

#### Reference: Service Discovery & CoreDNS Mechanics

| Component | Role | Mechanism |
|---|---|---|
| **CoreDNS** | In-cluster DNS server | Resolves service hostnames to stable virtual `ClusterIP` addresses using Kubernetes API watcher. |
| **`/etc/resolv.conf`** | Container DNS resolver config | Injected into every pod with search domains: `<ns>.svc.cluster.local`, `svc.cluster.local`, `cluster.local`. |
| **ClusterIP** | Virtual stable IP | A durable VIP allocated from the service CIDR block that never changes for the lifetime of the Service. |
| **EndpointSlice / iptables** | Data-plane load balancer | Translates the ClusterIP VIP to actual healthy Pod IPs via Linux kernel DNAT rules. |

> **DNS Resolution Walkthrough:**
> 1. `orders` executes HTTP GET to `http://catalog:8080/products`.
> 2. The Linux resolver reads `/etc/resolv.conf` and appends the first search domain: `catalog` &rarr; `catalog.shop.svc.cluster.local`.
> 3. CoreDNS answers with the ClusterIP (e.g., `10.96.164.136`).
> 4. The Linux network stack transmits TCP packets to `10.96.164.136:8080`.
> 5. `kube-proxy` (via iptables/IPVS) intercepts the packets and load balances them across ready catalog Pod endpoints (`10.244.0.x`).

**Key Takeaways & Answers:**
- **Runtime Location:** `orders` resolves `catalog` by relying on Kubernetes CoreDNS and the pod's search domain list. It doesn't need to know individual pod IPs or manage service registries (like Eureka or Consul).
- **Full Qualified Domain Name (FQDN):** `catalog.shop.svc.cluster.local` (Syntax: `<service-name>.<namespace>.svc.<cluster-domain>`).
- **Pod IP Churn Immunity:** The Service's `ClusterIP` and DNS record are permanent. When pods die and restart with new IPs, Kubernetes updates the `EndpointSlice` backing the service. The client continues sending traffic to the exact same ClusterIP.
- **Cross-Namespace Calls:** If `catalog` moved to an `inventory` namespace, the short name `catalog` would resolve to `catalog.shop...` and fail (NXDOMAIN). The URL would have to be updated to `http://catalog.inventory:8080` or `http://catalog.inventory.svc.cluster.local:8080`.

<!-- Your notes go here -->

---

## Probes & Health Checks

> 🤚 Fill this in after **Playbook 06** → [06-operations.md](../playbooks/06-operations.md)

**Think through:**
- What is the difference between a liveness probe and a readiness probe?
- What does Kubernetes do when a readiness probe fails? What about a liveness probe?
- Why would you make a readiness probe stricter than a liveness probe?
- What did you observe in `kubectl get pods` when you forced the readiness failure?

#### Reference: Kubernetes Probe Lifecycle & Spring Boot Actuator

| Probe Type | Operational Question | Target State | Failure Action | Spring Boot Path |
|---|---|---|---|---|
| **`startupProbe`** | *"Has slow initialization completed?"* | App booting up | Inhibits liveness/readiness; restarts pod if grace period expires | `/actuator/health/liveness` |
| **`livenessProbe`** | *"Is the process deadlocked or corrupted?"* | Steady-state vitality | Kubelet terminates and restarts the container (`restarts++`) | `/actuator/health/liveness` |
| **`readinessProbe`** | *"Can the pod accept customer traffic right now?"* | In-flight capacity | Removes Pod IP from Service `EndpointSlice`; **no restart** | `/actuator/health/readiness` |

**Key Takeaways & Answers:**
- **Liveness vs. Readiness:** Liveness gates container *vitality* (restarting broken or deadlocked processes). Readiness gates network *traffic admission* (routing requests only to containers capable of fulfilling them).
- **Failure Consequences:** When a readiness probe fails, the pod transitions to `0/1 Ready` (`Running` status) and is removed from the Service endpoints, stopping customer requests from hitting it. When a liveness probe fails, kubelet issues a `SIGTERM`/`SIGKILL` to restart the container.
- **Why Readiness is Stricter:** Readiness should verify dependencies (e.g. database connectivity, cache warming, queue saturation). If external database hiccups caused liveness to fail, all application replicas would reboot simultaneously in a catastrophic cascading failure. Keeping dependency health in readiness isolates traffic while preserving running JVMs.
- **Forced Failure Observation:** In `kubectl get pods -n shop`, the targeted pod transitioned to `0/1 Ready`, but remained in `Running` status with `RESTARTS: 0`. Checking `kubectl get endpoints catalog -n shop` confirmed that its IP was immediately purged from active routing, while the second healthy pod continued serving traffic without a single dropped packet.

---

## Jobs

> 🤚 Fill this in after **Playbook 06** → [06-operations.md](../playbooks/06-operations.md)

**Think through:**
- When would you use a Job instead of a Deployment?
- What is the difference between `restartPolicy: OnFailure` and `restartPolicy: Never`?
- What happened to the Job pod after it completed successfully?
- What is a CronJob and when would you use one?

#### Reference: Batch Workload Semantics

| Workload Controller | Lifecycle Model | Termination Expectation | Failure Handling |
|---|---|---|---|
| **Deployment** | Continuous Daemon | Never terminates; non-zero or zero exit code triggers restart | Replaces dead pods to maintain desired replica count |
| **Job** | Run-to-Completion Batch | Must exit with code `0` (`Completed`) | Retries up to `backoffLimit` before marking Job `Failed` |
| **CronJob** | Scheduled Recurring Job | Creates temporary Jobs on a cron schedule (`*/15 * * * *`) | Spawns discrete Jobs per schedule trigger |

**Key Takeaways & Answers:**
- **Job vs. Deployment:** Use a Job for finite tasks (schema migrations, initial data seeding, batch report generation, one-off backups). Using a Deployment for migrations leads to race conditions when multiple pods execute DDL simultaneously or infinite restart loops when the container exits.
- **`restartPolicy: OnFailure` vs. `Never`:** `OnFailure` restarts the container within the *same* Pod instance upon exit code $\neq 0$ (preserving local pod identity and volume state). `Never` terminates the failed Pod and instructs the Job controller to schedule a *new* Pod.
- **Post-Completion Pod Retention:** After the Job succeeded, the pod transitioned to `Completed` (`0/1 Ready`). Kubernetes intentionally does not delete completed Job pods, allowing operators to run `kubectl logs` for auditing.
- **CronJob:** A CronJob is a higher-level controller that creates Jobs based on a crontab schedule (e.g., `0 2 * * *` for 2 AM daily). Ideal for periodic backups, nightly syncs, and maintenance tasks.

---

## Rolling Updates & Rollbacks

> 🤚 Fill this in after **Playbook 06** → [06-operations.md](../playbooks/06-operations.md)

**Think through:**
- How does Kubernetes ensure zero downtime during a rolling update?
- What do `maxSurge` and `maxUnavailable` control, and how do they trade off speed vs. risk?
- How did `kubectl rollout undo` work? What did it actually change under the hood?
- At what point during a rolling update is traffic still being served from the old pods?

#### Reference: Deployment Rolling Strategy & Rollback Architecture

| Parameter | Default | Production Value for Zero Downtime | Architectural Impact |
|---|---|---|---|
| **`maxSurge`** | `25%` | `1` (or `25%`) | Maximum number of extra Pods scheduled above desired count during rollout. |
| **`maxUnavailable`** | `25%` | `0` | Guarantees that the number of available pods never drops below the desired replica count. |
| **`revisionHistoryLimit`** | `10` | `10` | Number of old ReplicaSets retained in etcd for instant rollback capability. |

**Key Takeaways & Answers:**
- **Zero-Downtime Guarantee:** Kubernetes orchestrates dual ReplicaSets. A new `v2` pod is created alongside `v1` pods. The `v1` pod is only decommissioned *after* the `v2` pod passes its `readinessProbe` and is admitted into the Service `EndpointSlice`.
- **`maxSurge` vs. `maxUnavailable`:** `maxSurge` controls how many additional pods can run during rollout (requires spare cluster capacity). `maxUnavailable` controls how many pods can be offline. Setting `maxUnavailable: 0` ensures zero drop in capacity at the expense of rollout speed.
- **`kubectl rollout undo` Mechanics:** `rollout undo` does not build artifacts or alter files; it updates the Deployment's `spec.template` to match the exact Pod template from a previous ReplicaSet revision (tracked via annotations).
- **Traffic Handover Point:** Old pods continue serving live traffic throughout the update. As new pods become `Ready`, traffic is balanced across both versions until old pods are systematically drained, sent `SIGTERM`, and terminated.


---

## Kustomize

> 🤚 Fill this in after **Playbook 07** → [07-kustomize-gke.md](../playbooks/07-kustomize-gke.md)

#### Reference: Kustomize Architecture & Multi-Cloud Portability

| Dimension | Raw YAML Duplication | Helm | Kustomize |
|---|---|---|---|
| **Mechanism** | Copy/paste directory per env | Go text templating (`{{ .Values... }}`) | Base + targeted overlay patches |
| **Tooling** | None | Requires separate `helm` CLI | Built directly into `kubectl` (`-k`) |
| **Drift Risk** | High (copies get out of sync) | Low | Low (single source of truth in `base/`) |
| **Readability** | High per file | Lower (YAML is broken by template directives) | High (pure, valid YAML at all layers) |
| **Best For** | One-off prototypes | Third-party charts, public packages | In-house microservices across dev/cloud envs |

**Key Takeaways & Answers:**
- **Problem Solved:** Prevents configuration drift by keeping 95%+ of your Kubernetes YAML in a single, reusable `base/`. Environment differences (image paths, storage classes, replica counts) live only as surgical diffs in `overlays/`.
- **Base vs. Overlay:** `base/` holds complete, valid, environment-agnostic YAML. `overlays/<target>/` imports the base and declares only what is different using transformers (like `images:`) or patches.
- **The Diff Payoff:** `diff <(kubectl kustomize overlays/kind/) <(kubectl kustomize overlays/gke/)` reveals the exact differences between local and cloud: only the image registry URL and the StorageClass name (`standard` vs `standard-rwo`).
- **Kustomize vs. Helm:** Choose Kustomize for first-party microservices where you want native `kubectl` integration without the complexity of template languages. Choose Helm when packaging reusable software for distribution to external consumers or installing third-party vendor applications.

#### Reference: The Laptop-to-Cloud Transition (Production Lessons)

When transitioning from local clusters (`kind`) to production cloud Kubernetes (GKE/EKS), four architectural boundaries emerge:

1. **Multi-Architecture Builds (`arm64` vs `amd64`):**
   - Developer laptops often run Apple Silicon (`arm64`), whereas cloud Kubernetes nodes run Intel/AMD (`linux/amd64`).
   - Pushing an `arm64` image to a cloud cluster causes kubelet to fail with `no match for platform in manifest: not found`.
   - **Production Solution:** In CI/CD pipelines, images are compiled for `linux/amd64` or multi-arch manifest lists (`docker buildx build --platform=linux/amd64,linux/arm64`). In multi-stage builds, using `FROM --platform=$BUILDPLATFORM` for the compiler stage avoids slow and unstable QEMU CPU emulation.

2. **Probe Lifecycle & `startupProbe`:**
   - On burstable cloud compute with memory/CPU limits, heavy runtime stacks (like Java, Spring Boot, or .NET) take 30–60 seconds to cold start and connect to databases.
   - A `livenessProbe` with short initial delay will repeatedly kill the container before it finishes booting (`Exit Code 143`).
   - **Production Solution:** Always define a `startupProbe` for slow-starting applications. It polls up to several minutes without triggering pod kills, and seamlessly hands over to liveness/readiness probes once the app is healthy.

3. **Strategic Merge Patches on PVC Arrays:**
   - Kustomize patches merge dictionaries, but replace list elements without unique merge keys.
   - Patching a StatefulSet's `volumeClaimTemplates` to change `storageClassName` will overwrite `accessModes` and `resources` if they are not explicitly specified in the patch.

4. **Public Exposure (Secure by Default):**
   - Kubernetes services default to `ClusterIP` to protect internal databases and microservices from public exposure.
   - Public access requires an explicit `LoadBalancer` (allocating cloud IPs) or an `Ingress`/`Gateway API` controller providing layer-7 host and path routing with TLS termination.

<!-- Your notes go here -->

---

## Deployment vs. StatefulSet: The Architect's View

> 🤚 Fill this in after **Playbook 07** — this is the capstone comparison.

Complete the table from memory before checking your notes:

| Dimension | Deployment | StatefulSet |
|---|---|---|
| **Pod naming** | Random hash suffix (`catalog-7bf89...`) | Deterministic ordinal index (`postgres-0`, `postgres-1`) |
| **Pod identity across restarts** | Ephemeral; replacement gets a completely new name & IP | Stable; replacement receives the exact same ordinal and identity |
| **Storage** | Shared volume (`EmptyDir`, PVC shared across replicas) | Dedicated per-pod storage via `volumeClaimTemplates` (`postgres-data-postgres-0`) |
| **Startup / shutdown ordering** | Parallel, nondeterministic | Strictly ordered (`0` before `1` on start; reverse on shutdown) |
| **Primary use case** | Stateless apps (REST APIs, workers, web servers) | Stateful workloads (Databases, Kafka, Redis, Zookeeper) |

**In one paragraph:** Why does running a database as a Deployment instead of a StatefulSet lead to data loss?
Deployments treat Pods as fungible, disposable cattle. If a database Deployment scales or restarts, multiple pods may attach concurrently to the same storage without lock coordination, causing filesystem corruption. Furthermore, when a pod in a Deployment dies, its replacement does not inherit the previous pod's unique identity or volume binding. StatefulSets guarantee stable network identities and dedicate isolated, durable `PersistentVolumeClaims` to each specific ordinal instance (`postgres-0`), ensuring data persists safely across pod rescheduling and restarts.

<!-- Your notes go here -->
