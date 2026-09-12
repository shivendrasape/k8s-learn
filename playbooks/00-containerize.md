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

### 1. Create the `kind` Cluster

```bash
# Create a cluster named k8s-learn
kind create cluster --name k8s-learn

# Verify the cluster is running and kubectl is pointed at it
kubectl cluster-info --context kind-k8s-learn
```

### 2. Build the `catalog` Application

```bash
# Navigate to the catalog app and build the JAR
cd apps/catalog
./mvnw clean package -DskipTests

# Build the Docker image
docker build -t shop/catalog:dev .
```

### 3. Build the `orders` Application

```bash
# Navigate to the orders app and build the JAR
cd apps/orders
./mvnw clean package -DskipTests

# Build the Docker image
docker build -t shop/orders:dev .
```

### 4. Load Images into `kind`

```bash
# Load both images into the kind cluster
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
