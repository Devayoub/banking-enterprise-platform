#!/usr/bin/env bash
# Helper for the local infrastructure stack.
#   ./docker/stack.sh up       start all services and wait until they are healthy
#   ./docker/stack.sh down     stop services (keeps data)
#   ./docker/stack.sh reset    stop services and DELETE all local data volumes
#   ./docker/stack.sh status   show container health
#   ./docker/stack.sh logs [service]
set -euo pipefail

cd "$(dirname "$0")"
COMPOSE=(docker compose -f docker-compose.yml)
[[ -f .env ]] && COMPOSE+=(--env-file .env)

case "${1:-}" in
  up)
    "${COMPOSE[@]}" up -d --wait postgres keycloak kafka activemq
    "${COMPOSE[@]}" up kafka-init --exit-code-from kafka-init
    echo
    echo "Keycloak admin console : http://localhost:8180/admin   (banking-realm)"
    echo "ActiveMQ web console   : http://localhost:8161/admin"
    echo "Kafka bootstrap        : localhost:9092"
    echo "PostgreSQL             : localhost:5432 (core_banking, keycloak, camunda)"
    ;;
  down)
    "${COMPOSE[@]}" down
    ;;
  reset)
    read -r -p "This deletes all local PostgreSQL, Kafka and ActiveMQ data. Continue? [y/N] " answer
    [[ "$answer" =~ ^[Yy]$ ]] || exit 1
    "${COMPOSE[@]}" down --volumes --remove-orphans
    ;;
  status)
    "${COMPOSE[@]}" ps
    ;;
  logs)
    shift
    "${COMPOSE[@]}" logs -f "$@"
    ;;
  *)
    echo "Usage: $0 {up|down|reset|status|logs [service]}" >&2
    exit 1
    ;;
esac
