#!/usr/bin/env bash
# Genera un "JWT" de prueba para Swagger/curl en local.
#
# JwtClaimsFilter confía en que el API Gateway ya validó la firma, así que solo
# decodifica el claim "sub" sin verificarla. Por eso, para pruebas locales alcanza con
# un token con la forma correcta (header.payload.firma) sin firmar de verdad.
#
# Uso: ./scripts/generate-test-jwt.sh [uuid-del-usuario]
set -euo pipefail

USER_ID="${1:-11111111-1111-1111-1111-111111111111}"

b64url() {
    openssl base64 -A | tr '+/' '-_' | tr -d '='
}

HEADER=$(printf '{"alg":"none","typ":"JWT"}' | b64url)
PAYLOAD=$(printf '{"sub":"%s"}' "$USER_ID" | b64url)

echo "${HEADER}.${PAYLOAD}."
