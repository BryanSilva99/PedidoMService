#!/usr/bin/env bash
set -Eeuo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
export ORDERS_DEPLOY_IMAGE="${1:?Uso: deploy-local.sh orders-service:COMMIT}"
env_file="${ORDERS_ENV_FILE:-/home/jesibel/PedidoMService/.env}"
test -r "$env_file" || { echo "No se puede leer $env_file" >&2; exit 1; }
docker image inspect "$ORDERS_DEPLOY_IMAGE" >/dev/null

# El proyecto y volumen mantienen los nombres del despliegue existente.
compose=(docker compose --project-name orders-demo --env-file "$env_file"
  -f "$project_dir/compose.yaml" -f "$project_dir/compose.deploy.yaml")
"${compose[@]}" config --quiet
mysql_id="$("${compose[@]}" ps -q mysql)"
test -n "$mysql_id" && test "$(docker inspect --format '{{.State.Health.Status}}' "$mysql_id")" = healthy || {
  echo 'MySQL debe estar iniciado y healthy antes del despliegue.' >&2
  exit 1
}
previous_id="$("${compose[@]}" ps -q orders-service)"
previous_image=''
if test -n "$previous_id"; then
  previous_image="$(docker inspect --format '{{.Image}}' "$previous_id")"
fi

check_api() {
  for attempt in {1..45}; do
    if python3 -c 'import json,urllib.request
with urllib.request.urlopen("http://127.0.0.1:8080/pedidos", timeout=3) as response:
    assert response.status == 200 and isinstance(json.load(response), list)' 2>/dev/null; then
      return 0
    fi
    sleep 2
  done
  return 1
}

if "${compose[@]}" up -d --no-deps --no-build --force-recreate orders-service && check_api; then
  echo "Desplegado $ORDERS_DEPLOY_IMAGE; GET /pedidos HTTP 200 y JSON válido."
else
  "${compose[@]}" logs --tail=80 orders-service || true
  if test -n "$previous_image"; then
    echo 'El despliegue falló. Restaurando imagen anterior.' >&2
    export ORDERS_DEPLOY_IMAGE="$previous_image"
    if "${compose[@]}" up -d --no-deps --no-build --force-recreate orders-service && check_api; then
      echo 'Imagen anterior restaurada.' >&2
    else
      echo 'La restauración también falló; revisar Orders manualmente.' >&2
    fi
  fi
  exit 1
fi
