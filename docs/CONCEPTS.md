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

## Pods

> 🤚 Fill this in after **Playbook 01** → [01-basics.md](../playbooks/01-basics.md)

**Think through:**
- What is a Pod, and what does it actually contain at runtime?
- Why does Kubernetes use Pods instead of running containers directly?
- What happens to a Pod when the node it is running on dies?
- When would you ever put *two* containers in the same Pod?

<!-- Your notes go here -->

---

## ReplicaSets & Deployments

> 🤚 Fill this in after **Playbook 01** → [01-basics.md](../playbooks/01-basics.md)

**Think through:**
- What problem does a ReplicaSet solve that a bare Pod cannot?
- If you delete a Pod that is managed by a Deployment, what happens and why?
- What is "desired state" vs "actual state"? Why does that distinction matter in operations?
- Why do you almost never create a ReplicaSet directly?

<!-- Your notes go here -->

---

## Services & DNS

> 🤚 Fill this in after **Playbook 02** → [02-networking.md](../playbooks/02-networking.md)

**Think through:**
- What problem does a ClusterIP Service solve? Why can you not just use a Pod's IP directly?
- How does `curl http://catalog:8080` resolve inside the cluster? Who resolves `catalog`?
- What is the difference between a ClusterIP Service and a NodePort Service?
- What is the relationship between a Service's `selector` field and pod labels?

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
