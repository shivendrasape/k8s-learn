# Playbook 00: Build & Containerize

## Architect's Concept
Before Kubernetes can orchestrate your workloads, those workloads need to exist as **container images**. This playbook bridges the gap between source code and the K8s manifests used in later playbooks.

**Key insight:** `kind` runs a full Kubernetes cluster inside Docker containers. It cannot pull from your local Docker daemon by default — you must explicitly **load** images into the cluster with `kind load docker-image`.

*Boundary Check:* We build images locally and load them into `kind`. No remote registry is involved until Playbook 07 (GKE).

## Prerequisites

Verify all tools are installed (see the spec's Prerequisites section):

```bash
docker --version && kind --version && kubectl version --client && java -version && mvn -version
```

## Implementation Steps

### 1. Understanding the Build Context and `.dockerignore`

When you run `docker build [context_path]`:
- The Docker CLI packages everything in `context_path` and sends it to the Docker Daemon as the **build context**.
- **Why don't we need the host's `target/` folder in the Docker image?**
  There are two contrasting container build patterns:
  1. *Host-dependent (Single-stage):* You compile on your host Mac (`mvn package`), and the Dockerfile does `COPY target/*.jar app.jar`. In this pattern, `target/` is needed, but your builds are brittle (relies on host JDK, host OS settings, and can fail across different machines).
  2. *Self-contained (Multi-stage — our approach):*
     - **Stage 1 (`builder`):** Copies *only* source code (`src/`) and Maven configs (`pom.xml`, wrapper). Docker executes `./mvnw clean package` **inside an isolated Linux container**.
     - This produces a clean, reproducible `target/` folder **inside the builder container filesystem** (`/workspace/target/`).
     - **Stage 2 (`runtime`):** Docker copies *only* the single fat JAR (`COPY --from=builder /workspace/target/*.jar app.jar`) into the slim JRE image.
     - **Result:** The host's local `target/` folder is completely redundant. If `.dockerignore` didn't exclude it, Docker would waste time tarring and transmitting tens of megabytes of host binaries that Stage 1 ignores anyway. Furthermore, runtime images never need the extra debris in `target/` (thousands of `.class` files, build logs, and compiler caches).

---

### 2. Build the Container Images & Understanding Image Tags

#### Anatomy of a Docker Image Name & Tag
Docker image references follow this structure:
```
[registry_host[:port]/][namespace_or_org/]repository:tag
```

| Type | Example | What it means |
|------|---------|---------------|
| **Official Docker Hub** | `postgres:16-alpine` | Implicit registry `docker.io/library`. Pulled automatically from public Docker Hub. |
| **Cloud Registry (GKE)** | `asia-docker.pkg.dev/my-project/shop/catalog:1.0.0` | Remote image stored in Google Artifact Registry; requires authentication. |
| **Local-Only Tag** | `shop/catalog:dev` | Stored exclusively in your local Docker daemon. No network upload or download. |

#### Why use a local tag like `shop/catalog:dev`?
1. **Local identification:** `shop` is a project namespace, `catalog` is the service, and `:dev` indicates a local iteration.
2. **Avoiding the `:latest` trap in Kubernetes:**
   - If you tag an image `:latest` (or omit the tag), Kubernetes automatically defaults `imagePullPolicy: Always`.
   - On a local `kind` cluster, Kubernetes would try to reach Docker Hub to pull `shop/catalog:latest`, fail with `ErrImagePull`, and ignore the locally loaded image.
   - By giving it a specific local tag like `:dev`, we can use `imagePullPolicy: IfNotPresent` or `Never`, telling Kubernetes to run the image already present on the cluster node.

```bash
# Build the catalog image
docker build -t shop/catalog:dev apps/catalog/

# Build the orders image
docker build -t shop/orders:dev apps/orders/
```

#### Command Breakdown:
- `docker build`: Command to assemble a container image from a `Dockerfile`.
- `-t shop/catalog:dev`: Tag flag (`-t` / `--tag`). Assigns the name `shop/catalog` with tag `:dev`.
- `apps/catalog/`: The directory containing the `Dockerfile` and source files (the **build context**).

Inspect the resulting images and sizes:
```bash
docker images | grep shop
```
*Notice how small the final image is: only the slim JRE and the fat JAR are present; the Maven compiler and dependencies from Stage 1 were discarded.*

---

### 3. Smoke Test Containers Locally

Before loading images into Kubernetes, verify that the containers start and respond to HTTP traffic:

```bash
# Test catalog on port 8080
docker run --rm -p 8080:8080 shop/catalog:dev
```
In another terminal tab:
```bash
curl http://localhost:8080/
# Expected: {"service":"catalog","status":"ok"}
```
*(Press Ctrl+C to stop the container)*

```bash
# Test orders on port 8083
docker run --rm -p 8083:8083 shop/orders:dev
```
In another terminal tab:
```bash
curl http://localhost:8083/
# Expected: {"service":"orders","status":"ok"}
```
*(Press Ctrl+C to stop the container)*

#### Command Breakdown:
- `docker run`: Creates and starts a container process from an image.
- `--rm`: Automatically removes the container and its file system when it stops. Prevents stopped container clutter on your machine.
- `-p 8080:8080` (or `-p 8083:8083`): Port publishing flag (`-p <host_port>:<container_port>`). Forwards incoming traffic from your Mac's localhost port to the container's internal listening port.
- `shop/catalog:dev`: The image to run.

---

### 4. Create the `kind` Cluster

```bash
# Create a cluster named k8s-learn
kind create cluster --name k8s-learn

# Verify the cluster is running and kubectl is pointed at it
kubectl cluster-info --context kind-k8s-learn
```

---

### 5. Load Images into `kind`

Because our images are local-only (`shop/*:dev`) and not published to Docker Hub or any remote registry, `kind`'s container runtime cannot pull them. We must load them into the kind cluster nodes:

```bash
kind load docker-image shop/catalog:dev --name k8s-learn
kind load docker-image shop/orders:dev --name k8s-learn
```

> **Note:** The official `postgres` image will be pulled directly from Docker Hub by `kind` when we create the StatefulSet in Playbook 03. No manual loading is needed for public images.

## Verify & Prove

```bash
# Confirm the images are available inside the kind cluster's nodes
docker exec -it k8s-learn-control-plane crictl images | grep shop

# Expected output:
# docker.io/shop/catalog    dev    <hash>    <size>
# docker.io/shop/orders     dev    <hash>    <size>
```

## What's Next

With images loaded into the cluster, proceed to [Playbook 01: The Basics](01-basics.md) to create your first Deployment.

## Rebuild Workflow (Reference)

After making code changes, re-run this cycle:

```bash
# Rebuild → Reload → Restart
./mvnw clean package -DskipTests -f apps/catalog/pom.xml
docker build -t shop/catalog:dev apps/catalog/
kind load docker-image shop/catalog:dev --name k8s-learn
kubectl rollout restart deployment/catalog -n shop
```
