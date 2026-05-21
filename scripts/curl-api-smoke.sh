#!/usr/bin/env bash
# BeautySkin API smoke test — API at http://localhost:8080
set -uo pipefail
BASE="${API_BASE:-http://localhost:8080/api}"
TMP="${TMPDIR:-/tmp}/bs-api-test"
mkdir -p "$TMP"
RESP="$TMP/resp.json"
PASS=0
FAIL=0

json_field() {
  local file="$1" path="$2"
  [ -f "$file" ] || return 0
  node -e "
    const fs=require('fs');
    const d=JSON.parse(fs.readFileSync(process.argv[1],'utf8'));
    const keys=process.argv[2].split('.').filter(Boolean);
    let v=d;
    for (const k of keys) {
      if (Array.isArray(v) && /^[0-9]+$/.test(k)) v=v[parseInt(k,10)];
      else v=v?.[k];
    }
    if (v!==undefined && v!==null) console.log(String(v));
  " "$(cygpath -w "$file" 2>/dev/null || echo "$file")" "$path" 2>/dev/null \
    || sed -n 's/.*"id":"\([a-f0-9-]\{36\}\)".*/\1/p' "$file" | head -1
}

token_from() { sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p' "$1" | head -1; }

check() {
  local name="$1" method="$2" path="$3" expect="$4"
  shift 4
  local code
  code=$(curl -s -o "$RESP" -w "%{http_code}" -X "$method" "$@" "${BASE}${path}")
  if [ "$code" = "$expect" ]; then
    echo "  OK  [$code] $method $path — $name"
    PASS=$((PASS+1))
  else
    echo "  FAIL [$code ≠ $expect] $method $path — $name"
    head -c 280 "$RESP" 2>/dev/null; echo ""
    FAIL=$((FAIL+1))
  fi
}

login() {
  curl -s -o "$TMP/login.json" -X POST "$BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$1\",\"password\":\"$2\"}"
  token_from "$TMP/login.json"
}

echo "BeautySkin API curl smoke — $(date -Iseconds 2>/dev/null || date)"
echo "Base: $BASE"
echo ""

echo "── PUBLIC ──"
check "catalog featured" GET "/catalog/featured?page=1&limit=3" 200
check "catalog categories" GET "/catalog/categories" 200
check "products list" GET "/products?page=1&limit=5" 200
PRODUCT_ID=$(json_field "$RESP" "data.content.0.id")
if [ -n "$PRODUCT_ID" ]; then
  check "product detail" GET "/products/$PRODUCT_ID" 200
  check "product reviews" GET "/products/$PRODUCT_ID/reviews?page=1&limit=5" 200
else
  echo "  WARN no product id in list"
fi

echo ""
echo "── AUTH (login) ──"
for cred in "buyer@beautyskin.com:buyer123" "admin@beautyskin.com:admin123" "owner@beautyskin.com:owner123" "staff@beautyskin.com:staff123"; do
  email="${cred%%:*}"; pass="${cred##*:}"
  check "login $email" POST "/auth/login" 200 -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$pass\"}"
done

BUYER=$(login buyer@beautyskin.com buyer123)
ADMIN=$(login admin@beautyskin.com admin123)
OWNER=$(login owner@beautyskin.com owner123)
STAFF=$(login staff@beautyskin.com staff123)

echo ""
echo "── BUYER ──"
if [ -z "$BUYER" ]; then echo "  SKIP — no token"; else
  BH=(-H "Authorization: Bearer $BUYER")
  check "cart" GET "/cart" 200 "${BH[@]}"
  check "checkout quote" GET "/checkout/quote" 200 "${BH[@]}"
  check "users/me" GET "/users/me" 200 "${BH[@]}"
  check "users/me shipping" GET "/users/me/shipping" 200 "${BH[@]}"
  check "user preferences" GET "/users/me/preferences" 200 "${BH[@]}"
  check "notifications" GET "/notifications?page=1&limit=5" 200 "${BH[@]}"
  check "orders (customer)" GET "/orders?page=1&limit=5" 200 "${BH[@]}"
  check "chat rooms" GET "/chat/rooms" 200 "${BH[@]}"
  check "DM threads" GET "/messages/threads" 200 "${BH[@]}"
  if [ -n "$PRODUCT_ID" ]; then
    check "add cart item" POST "/cart/items" 200 "${BH[@]}" -H "Content-Type: application/json" \
      -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":1}"
    check "cart after add" GET "/cart" 200 "${BH[@]}"
  fi
fi

echo ""
echo "── ADMIN ──"
if [ -z "$ADMIN" ]; then echo "  SKIP — no token"; else
  AH=(-H "Authorization: Bearer $ADMIN")
  check "admin dashboard" GET "/admin/dashboard" 200 "${AH[@]}"
  check "shops list" GET "/shops?page=1&limit=5" 200 "${AH[@]}"
  SHOP_ID=$(json_field "$RESP" "data.content.0.id")
  check "customers" GET "/customers?page=1&limit=5" 200 "${AH[@]}"
  check "suppliers" GET "/suppliers?page=1&limit=5" 200 "${AH[@]}"
  if [ -n "$SHOP_ID" ]; then
    check "shop by id" GET "/shops/$SHOP_ID" 200 "${AH[@]}"
    CUSTOMER_ID=$(json_field "$RESP" "data.content.0.id" 2>/dev/null)
    curl -s -o "$RESP" "${AH[@]}" "$BASE/customers?page=1&limit=1"
    CUSTOMER_ID=$(json_field "$RESP" "data.content.0.id")
    if [ -n "$CUSTOMER_ID" ]; then
      check "customer detail" GET "/customers/$CUSTOMER_ID" 200 "${AH[@]}"
    fi
  fi
fi

echo ""
echo "── OWNER / MERCHANT ──"
if [ -z "$OWNER" ]; then echo "  SKIP — no token"; else
  OH=(-H "Authorization: Bearer $OWNER")
  if [ -z "${SHOP_ID:-}" ]; then
    curl -s -o "$RESP" "${OH[@]}" "$BASE/shops?page=1&limit=1"
    SHOP_ID=$(json_field "$RESP" "data.content.0.id")
  fi
  if [ -n "${SHOP_ID:-}" ]; then
    check "shop dashboard" GET "/dashboard?shopId=$SHOP_ID" 200 "${OH[@]}"
    check "analytics summary" GET "/analytics/summary?shopId=$SHOP_ID&range=30d" 200 "${OH[@]}"
    check "inventory" GET "/inventory?shopId=$SHOP_ID&page=1&limit=5" 200 "${OH[@]}"
    check "promotions" GET "/promotions?shopId=$SHOP_ID&page=1&limit=5" 200 "${OH[@]}"
    check "orders (shop)" GET "/orders?shopId=$SHOP_ID&page=1&limit=5" 200 "${OH[@]}"
    check "merchant products" GET "/products/merchant?shopId=$SHOP_ID&page=1&limit=5" 200 "${OH[@]}"
    check "pos receipts" GET "/pos/receipts?shopId=$SHOP_ID&page=1&limit=5" 200 "${OH[@]}"
    check "supplier purchases" GET "/supplier-purchases?shopId=$SHOP_ID&page=1&limit=5" 200 "${OH[@]}"
    check "revoke requests" GET "/revoke-requests?shopId=$SHOP_ID&page=1&limit=5" 200 "${OH[@]}"
    check "shop staff" GET "/shops/$SHOP_ID/users?page=1&limit=5" 200 "${OH[@]}"
    ORDER_ID=$(json_field "$RESP" "data.content.0.id")
    curl -s -o "$RESP" "${OH[@]}" "$BASE/orders?shopId=$SHOP_ID&page=1&limit=1"
    ORDER_ID=$(json_field "$RESP" "data.content.0.id")
    if [ -n "$ORDER_ID" ]; then
      check "order detail" GET "/orders/$ORDER_ID" 200 "${OH[@]}"
    fi
  else
    echo "  WARN no shopId for owner tests"
  fi
fi

echo ""
echo "── STAFF ──"
if [ -n "$STAFF" ] && [ -n "${SHOP_ID:-}" ]; then
  SH=(-H "Authorization: Bearer $STAFF")
  check "staff orders" GET "/orders?shopId=$SHOP_ID&page=1&limit=5" 200 "${SH[@]}"
  check "staff inventory" GET "/inventory?shopId=$SHOP_ID&page=1&limit=5" 200 "${SH[@]}"
fi

echo ""
echo "── SECURITY ──"
check "cart without auth (302 redirect to login)" GET "/cart" 302
check "admin dashboard as buyer (403)" GET "/admin/dashboard" 403 -H "Authorization: Bearer $BUYER"

echo ""
echo "════════════════════════════════════"
echo "TOTAL: $PASS passed, $FAIL failed"
echo "════════════════════════════════════"
[ "$FAIL" -eq 0 ]
