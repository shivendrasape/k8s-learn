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

<!-- Your notes go here -->

---

## Internal DNS & Cross-Service Communication

> 🤚 Fill this in after **Playbook 05** → [05-communication.md](../playbooks/05-communication.md)

**Think through:**
- How does `orders` locate `catalog` at runtime without knowing its IP address?
- What is the full DNS name for the `catalog` Service inside the `shop` namespace?
- If the catalog pod restarts and gets a new IP, why does `http://catalog:8080` still work?
- What would you need to change if you moved `catalog` to a different namespace?

<!-- Your notes go here -->

---

## Probes & Health Checks

> 🤚 Fill this in after **Playbook 06** → [06-operations.md](../playbooks/06-operations.md)

**Think through:**
- What is the difference between a liveness probe and a readiness probe?
- What does Kubernetes do when a readiness probe fails? What about a liveness probe?
- Why would you make a readiness probe stricter than a liveness probe?
- What did you observe in `kubectl get pods` when you forced the readiness failure?

<!-- Your notes go here -->

---

## Jobs

> 🤚 Fill this in after **Playbook 06** → [06-operations.md](../playbooks/06-operations.md)

**Think through:**
- When would you use a Job instead of a Deployment?
- What is the difference between `restartPolicy: OnFailure` and `restartPolicy: Never`?
- What happened to the Job pod after it completed successfully?
- What is a CronJob and when would you use one?

<!-- Your notes go here -->

---

## Rolling Updates & Rollbacks

> 🤚 Fill this in after **Playbook 06** → [06-operations.md](../playbooks/06-operations.md)

**Think through:**
- How does Kubernetes ensure zero downtime during a rolling update?
- What do `maxSurge` and `maxUnavailable` control, and how do they trade off speed vs. risk?
- How did `kubectl rollout undo` work? What did it actually change under the hood?
- At what point during a rolling update is traffic still being served from the old pods?

<!-- Your notes go here -->

---

## Kustomize

> 🤚 Fill this in after **Playbook 07** → [07-kustomize-gke.md](../playbooks/07-kustomize-gke.md)

**Think through:**
- What problem does Kustomize solve compared to maintaining two separate copies of YAML?
- What is the relationship between `k8s/base/` and `k8s/overlays/`?
- Looking at the diff between the kind overlay output and the GKE overlay output — what changed?
- When would you choose Kustomize over Helm?

<!-- Your notes go here -->

---

## Deployment vs. StatefulSet: The Architect's View

> 🤚 Fill this in after **Playbook 07** — this is the capstone comparison.

Complete the table from memory before checking your notes:

| Dimension | Deployment | StatefulSet |
|-----------|-----------|-------------|
| Pod naming | | |
| Pod identity across restarts | | |
| Storage | | |
| Startup / shutdown ordering | | |
| Primary use case | | |

**In one paragraph:** Why does running a database as a Deployment instead of a StatefulSet lead to data loss?

<!-- Your notes go here -->
