#!/bin/bash

set -euo pipefail

AWS_REGION="ap-northeast-2"
PARAMETER_STORE_PATH="/moa/prod/"
SECRET_ID="moa/secrets"

APP_ENV_DIR="/etc/moa-server"
APP_ENV_FILE="${APP_ENV_DIR}/app.env"

REQUIRED_KEYS=(
  JWT_SECRET
  REDIS_HOST
  REDIS_PORT
  DB_URL
  DB_PASSWORD
  DB_USERNAME
  COOKIE_SECURE
  CORS_ALLOWED_ORIGINS
  ACCESS_TOKEN_EXPIRATION
  REFRESH_TOKEN_EXPIRATION
  VAPID_PUBLIC_KEY
  VAPID_PRIVATE_KEY
  VAPID_SUBJECT
)

command -v aws >/dev/null 2>&1 || {
  echo "AWS CLI가 설치되어 있지 않습니다." >&2
  exit 1
}

command -v jq >/dev/null 2>&1 || {
  echo "jq가 설치되어 있지 않습니다." >&2
  exit 1
}

install -d -o root -g root -m 700 "$APP_ENV_DIR"

umask 077
TEMP_ENV_FILE=$(mktemp "${APP_ENV_FILE}.tmp.XXXXXX")

cleanup() {
  rm -f "$TEMP_ENV_FILE"
}

trap cleanup EXIT

# Parameter Store
aws ssm get-parameters-by-path \
  --region "$AWS_REGION" \
  --path "$PARAMETER_STORE_PATH" \
  --recursive \
  --with-decryption \
  --output json |
jq -r '
  .Parameters[]
  | (.Name | split("/") | last) as $key
  | select($key | test("^[A-Za-z_][A-Za-z0-9_]*$"))
  | "\($key)=\(.Value | @json)"
' >> "$TEMP_ENV_FILE"

# Secrets Manager
aws secretsmanager get-secret-value \
  --region "$AWS_REGION" \
  --secret-id "$SECRET_ID" \
  --output json |
jq -r '
  .SecretString
  | fromjson
  | to_entries[]
  | select(.key | test("^[A-Za-z_][A-Za-z0-9_]*$"))
  | "\(.key)=\(.value | tostring | @json)"
' >> "$TEMP_ENV_FILE"

# Parameter Store와 Secrets Manager의 key 중복 검사
DUPLICATE_KEYS=$(
  cut -d= -f1 "$TEMP_ENV_FILE" |
  sort |
  uniq -d
)

if [[ -n "$DUPLICATE_KEYS" ]]; then
  echo "중복된 환경변수가 있습니다:" >&2
  echo "$DUPLICATE_KEYS" >&2
  exit 1
fi

# 필수 환경변수 존재 여부와 빈 값 검사
for key in "${REQUIRED_KEYS[@]}"; do
  line=$(grep -m1 "^${key}=" "$TEMP_ENV_FILE" || true)

  if [[ -z "$line" ]]; then
    echo "필수 환경변수가 없습니다: ${key}" >&2
    exit 1
  fi

  if [[ "$line" == "${key}=\"\"" ]]; then
    echo "필수 환경변수의 값이 비어 있습니다: ${key}" >&2
    exit 1
  fi
done

chown root:root "$TEMP_ENV_FILE"