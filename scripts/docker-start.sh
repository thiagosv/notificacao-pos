#!/usr/bin/env bash
set -euo pipefail

# scripts/docker-start.sh
# Script para orquestrar startup da infra e serviços usando o docker-compose do repositório.
# Uso:
#   ./scripts/docker-start.sh infra     # sobe infra (postgres, mongo, redis, zookeeper, kafka, wiremock, kafka-ui)
#   ./scripts/docker-start.sh services  # sobe serviços (quota-service, notification-core, provider-push, api-gateway)
#   ./scripts/docker-start.sh all       # infra + services
#   ./scripts/docker-start.sh stop      # derruba tudo
#   ./scripts/docker-start.sh status    # mostra status dos containers gerenciados
#   ./scripts/docker-start.sh rebuild   # rebuilda um service
#   ./scripts/docker-start.sh logs <service>  # segue logs de um container

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="docker-compose.yml"
DC="docker compose -f $COMPOSE_FILE"

INFRA_CONTAINERS=(
  postgres
  mongodb
  redis
  zookeeper
  kafka
  kafka-init
  kafka-ui
  wiremock
  prometheus
  grafana
)

SERVICE_CONTAINERS=(
  quota-service
  notification-core
  provider-push
  provider-email
  provider-sms
  api-gateway
  audit-service
)

# Timeout in seconds for waiting health
DEFAULT_TIMEOUT=30
SLEEP=3

function die() {
  echo "ERROR: $*" >&2
  exit 1
}

function check_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    die "docker is not installed or not in PATH"
  fi
}

function wait_for_container_healthy() {
  local container="$1"
  local timeout=${2:-$DEFAULT_TIMEOUT}
  local waited=0

  echo "Waiting for $container to be healthy (timeout=${timeout}s) ..."
  while true; do
    if ! docker ps -a --format '{{.Names}}' | grep -qx "$container"; then
      # container not present yet
      sleep $SLEEP
      waited=$((waited + SLEEP))
      if [ $waited -ge $timeout ]; then
        die "$container did not appear within ${timeout}s"
      fi
      continue
    fi

    # Check Health if available
    local health
    health=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container" 2>/dev/null || true)

    # If container has Health, expect 'healthy'. Otherwise expect 'running'.
    if [ "$health" = "healthy" ] || [ "$health" = "running" ]; then
      echo "$container -> $health"
      break
    fi

    # If status shows 'exited' or 'unhealthy' fail fast
    if [ "$health" = "unhealthy" ] || [ "$health" = "exited" ]; then
      docker logs --tail 200 "$container" || true
      die "$container reached status: $health"
    fi

    sleep $SLEEP
    waited=$((waited + SLEEP))
    if [ $waited -ge $timeout ]; then
      die "$container did not become healthy within ${timeout}s (last status: $health)"
    fi
  done
}

function up_infra() {
  echo "Bringing up infra containers..."
  $DC up -d ${INFRA_CONTAINERS[*]}

  # Wait for each infra container to be healthy (or running)
  for c in "${INFRA_CONTAINERS[@]}"; do
    wait_for_container_healthy "$c"
  done

  echo "Infra ready."
}

function up_services() {
  echo "Bringing up services..."
  # Ensure infra is up before starting services
  for c in "${INFRA_CONTAINERS[@]}"; do
    if ! docker ps -a --format '{{.Names}}' | grep "$c"; then
      die "Infra container $c not present. Run './scripts/docker-start.sh infra' first or use 'all'"
    fi
  done

  $DC up -d ${SERVICE_CONTAINERS[*]}

  # Wait for services to be healthy / running
  for c in "${SERVICE_CONTAINERS[@]}"; do
    wait_for_container_healthy "$c"
  done

  echo "Services ready."
}

function rebuild() {
  local svc="$1"
  if [ -z "$svc" ]; then
    die "rebuild requires a service name: usage: $0 rebuild <container-name>"
  fi
  docker-compose build "$svc"
  docker-compose up -d --force-recreate "$svc"
}

function stop_all() {
  echo "Stopping and removing all containers defined in compose..."
  $DC down
  docker volume prune -f >/dev/null 2>&1 || true
  docker volume rm -f notificacao-pos_kafka-data
}

function status() {
  echo "Docker compose ps for managed containers:"
  $DC ps
}

function logs_follow() {
  local svc="$1"
  if [ -z "$svc" ]; then
    die "logs requires a service name: usage: $0 logs <container-name>"
  fi
  docker logs -f --tail 200 "$svc"
}

function usage() {
  cat <<EOF
Usage: $(basename "$0") <command>
Commands:
  infra           Start infrastructure containers and wait for healthchecks
  services        Start services containers and wait for healthchecks (requires infra)
  all             Start infra then services
  rebuild         Rebild and restart a service container
  stop            Stop and remove containers
  status          Show docker-compose ps
  logs <service>  Tail logs for service container
EOF
}

# --------------------
# Entrypoint
# --------------------

if [ ${#@} -eq 0 ]; then
  usage
  exit 1
fi

check_docker

case "$1" in
  infra)
    up_infra
    ;;
  services)
    up_services
    ;;
  all)
    up_infra
    up_services
    ;;
  stop)
    stop_all
    ;;
  rebuild)
    shift
    rebuild "$@"
    ;;
  status)
    status
    ;;
  logs)
    shift
    logs_follow "$@"
    ;;
  *)
    usage
    exit 1
    ;;
esac

exit 0

