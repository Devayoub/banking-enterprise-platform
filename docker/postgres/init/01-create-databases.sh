#!/usr/bin/env bash
# Runs once, on the first start of an empty PostgreSQL volume.
# One database and one owner role per bounded context: core banking, Keycloak, Camunda.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
  -v core_pw="$CORE_DB_PASSWORD" \
  -v kc_pw="$KEYCLOAK_DB_PASSWORD" \
  -v camunda_pw="$CAMUNDA_DB_PASSWORD" <<'SQL'
CREATE ROLE core_banking LOGIN PASSWORD :'core_pw';
CREATE DATABASE core_banking OWNER core_banking;

CREATE ROLE keycloak LOGIN PASSWORD :'kc_pw';
CREATE DATABASE keycloak OWNER keycloak;

CREATE ROLE camunda LOGIN PASSWORD :'camunda_pw';
CREATE DATABASE camunda OWNER camunda;

REVOKE ALL ON DATABASE core_banking FROM PUBLIC;
REVOKE ALL ON DATABASE keycloak FROM PUBLIC;
REVOKE ALL ON DATABASE camunda FROM PUBLIC;
SQL
