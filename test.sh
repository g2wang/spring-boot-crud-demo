#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PRODUCTS_URL="${BASE_URL}/api/v1/products"

timestamp="$(date +%s)"
sku="PROD-${timestamp}"

created_response_file="$(mktemp)"
updated_response_file="$(mktemp)"

cleanup() {
  rm -f "$created_response_file" "$updated_response_file"
}
trap cleanup EXIT

print_step() {
  printf '\n==> %s\n' "$1"
}

extract_id() {
  sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p' "$1" | head -n 1
}

print_step "Create product"
curl -i -sS -X POST "$PRODUCTS_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"sku\": \"${sku}\",
    \"name\": \"Test Product ${timestamp}\",
    \"description\": \"Created by test.sh\",
    \"price\": 99.99,
    \"stockQuantity\": 10
  }" | tee "$created_response_file"

product_id="$(extract_id "$created_response_file")"
if [[ -z "$product_id" ]]; then
  echo "Could not extract created product id from response." >&2
  exit 1
fi

print_step "Get all products"
curl -i -sS "${PRODUCTS_URL}?page=0&size=10&sort=name,asc"

print_step "Search products"
curl -i -sS "${PRODUCTS_URL}?search=Test&page=0&size=5&sort=name,asc"

print_step "Get product by id: ${product_id}"
curl -i -sS "${PRODUCTS_URL}/${product_id}"

print_step "Update product by id: ${product_id}"
curl -i -sS -X PUT "${PRODUCTS_URL}/${product_id}" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Updated Test Product ${timestamp}\",
    \"description\": \"Updated by test.sh\",
    \"price\": 149.99,
    \"stockQuantity\": 20
  }" | tee "$updated_response_file"

# print_step "Delete product by id: ${product_id}"
# curl -i -sS -X DELETE "${PRODUCTS_URL}/${product_id}"

# print_step "Confirm deleted product returns 404"
# curl -i -sS "${PRODUCTS_URL}/${product_id}"
