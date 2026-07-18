#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KIND_CLUSTER_NAME="${KIND_CLUSTER_NAME:-telcox-local}"
SKIP_BUILD="${SKIP_BUILD:-false}"

cd "${ROOT_DIR}"

TELCOX_IMAGES=(
  telcox/discovery-server:1.0.0-SNAPSHOT
  telcox/config-server:1.0.0-SNAPSHOT
  telcox/api-gateway:1.0.0-SNAPSHOT
  telcox/bff-service:1.0.0-SNAPSHOT
  telcox/identity-service:1.0.0-SNAPSHOT
  telcox/customer-service:1.0.0-SNAPSHOT
  telcox/product-catalog-service:1.0.0-SNAPSHOT
  telcox/order-service:1.0.0-SNAPSHOT
  telcox/subscription-service:1.0.0-SNAPSHOT
  telcox/usage-service:1.0.0-SNAPSHOT
  telcox/billing-service:1.0.0-SNAPSHOT
  telcox/payment-service:1.0.0-SNAPSHOT
  telcox/notification-service:1.0.0-SNAPSHOT
  telcox/ticket-service:1.0.0-SNAPSHOT
  telcox/signal-atlas-web:0.1.0
)

COMPOSE_BUILD_SERVICES=(
  discovery-server
  config-server
  api-gateway
  bff-service
  identity-service
  customer-service
  product-catalog-service
  order-service
  subscription-service
  usage-service
  billing-service
  payment-service
  notification-service
  ticket-service
  signal-atlas-web
)

wait_for_rollouts() {
  for deployment in "$@"; do
    kubectl -n telcox-local rollout status "deploy/${deployment}" --timeout=600s
  done
}

if [ "${SKIP_BUILD}" != "true" ]; then
  mvn -B -ntp package -DskipTests
  docker compose build "${COMPOSE_BUILD_SERVICES[@]}"
fi

if command -v kind >/dev/null 2>&1 && kind get clusters | grep -qx "${KIND_CLUSTER_NAME}"; then
  for image in "${TELCOX_IMAGES[@]}"; do
    kind load docker-image "${image}" --name "${KIND_CLUSTER_NAME}"
  done
elif command -v minikube >/dev/null 2>&1 && minikube status >/dev/null 2>&1; then
  for image in "${TELCOX_IMAGES[@]}"; do
    minikube image load "${image}"
  done
else
  echo "No running Kind cluster named ${KIND_CLUSTER_NAME} or Minikube profile found; applying manifests with existing image access."
fi

kubectl apply -f k8s/local/namespace.yaml
kubectl apply -f k8s/local/configmap.yaml
kubectl apply -f k8s/local/secret.yaml

kubectl apply -f k8s/local/postgres.yaml
kubectl apply -f k8s/local/redis.yaml
kubectl apply -f k8s/local/kafka.yaml
wait_for_rollouts \
  identity-postgres customer-postgres product-postgres order-postgres subscription-postgres \
  usage-postgres billing-postgres payment-postgres notification-postgres ticket-postgres \
  keycloak-postgres redis kafka

kubectl apply -f k8s/local/discovery-server.yaml
kubectl apply -f k8s/local/config-server.yaml
wait_for_rollouts discovery-server config-server

kubectl apply -f k8s/local/platform-services.yaml
wait_for_rollouts keycloak kafka-connect zipkin mailhog prometheus grafana kafka-ui pgadmin signal-atlas-web

kubectl apply -f k8s/local/business-services.yaml
wait_for_rollouts \
  identity-service customer-service product-catalog-service order-service subscription-service \
  usage-service billing-service payment-service notification-service ticket-service

kubectl apply -f k8s/local/api-gateway.yaml
kubectl apply -f k8s/local/bff-service.yaml
wait_for_rollouts api-gateway bff-service
