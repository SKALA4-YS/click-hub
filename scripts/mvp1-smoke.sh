#!/usr/bin/env bash
set -euo pipefail

api_base_url="${API_BASE_URL:-http://localhost:8080}"
frontend_url="${FRONTEND_URL:-http://localhost:5173}"
jwt_secret="${CLICKHUB_JWT_SECRET:-local-development-secret-change-before-production-123456}"
demo_user_id="10000000-0000-0000-0000-000000000001"
demo_project_id="20000000-0000-0000-0000-000000000002"

case "$api_base_url" in
  http://localhost:*|http://127.0.0.1:*) ;;
  *) echo "This smoke script only runs against a local demo API." >&2; exit 2 ;;
esac

for command_name in curl jq node; do
  command -v "$command_name" >/dev/null || { echo "$command_name is required" >&2; exit 2; }
done

access_token="$(JWT_SECRET="$jwt_secret" JWT_SUBJECT="$demo_user_id" node -e '
  const crypto = require("crypto")
  const encode = (value) => Buffer.from(JSON.stringify(value)).toString("base64url")
  const now = Math.floor(Date.now() / 1000)
  const header = encode({ alg: "HS256" })
  const payload = encode({ sub: process.env.JWT_SUBJECT, iat: now, exp: now + 600 })
  const signature = crypto.createHmac("sha256", process.env.JWT_SECRET)
    .update(`${header}.${payload}`).digest("base64url")
  process.stdout.write(`${header}.${payload}.${signature}`)
')"
auth_header="Authorization: Bearer ${access_token}"

curl -fsS "$api_base_url/actuator/health" | jq -e '.status == "UP"' >/dev/null
curl -fsS "$api_base_url/api/v1/ping" | jq -e '.status == "ok"' >/dev/null
curl -fsS "$api_base_url/v1/feed" | jq -e '.success and (.data.items | length > 0)' >/dev/null
curl -fsS "$api_base_url/v1/tutorials" | jq -e '.success and (.data | length > 0)' >/dev/null
curl -fsS "$api_base_url/v1/insights/weekly" | jq -e '.success and (.data.headline | length > 0)' >/dev/null
curl -fsS -I "$frontend_url/" >/dev/null
curl -fsS -I "$frontend_url/projects/$demo_project_id" >/dev/null

curl -fsS -H "$auth_header" "$api_base_url/v1/users/me" \
  | jq -e --arg id "$demo_user_id" '.success and .data.id == $id' >/dev/null
curl -fsS -X PUT -H "$auth_header" "$api_base_url/v1/projects/$demo_project_id/like" \
  | jq -e '.success' >/dev/null
curl -fsS -X POST -H "$auth_header" -H 'Content-Type: application/json' \
  -d '{"body":"Automated local MVP smoke"}' \
  "$api_base_url/v1/projects/$demo_project_id/comments" | jq -e '.success' >/dev/null
curl -fsS -H "$auth_header" "$api_base_url/v1/projects/$demo_project_id/comments" \
  | jq -e '.success and any(.data[]; .body == "Automated local MVP smoke")' >/dev/null

allowed_origin="$(curl -fsS -D - -o /dev/null -X OPTIONS "$api_base_url/v1/feed" \
  -H "Origin: $frontend_url" -H 'Access-Control-Request-Method: GET' \
  | tr -d '\r' | awk -F': ' 'tolower($1) == "access-control-allow-origin" { print $2 }')"
test "$allowed_origin" = "$frontend_url"

echo "MVP1 local smoke passed: FE, BE, DB read/write, SPA rewrite, and CORS"
