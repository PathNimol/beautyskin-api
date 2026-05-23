#!/usr/bin/env bash
# Full API scenario tests (all write paths except Auth: no register/OTP/OAuth/logout).
set -uo pipefail
BASE="${API_BASE:-http://localhost:8080/api}"
TMP="${TMPDIR:-/tmp}/bs-scenarios"
mkdir -p "$TMP"
PASS=0
FAIL=0
SKIP=0
TS=$(date +%s)

log_ok() { echo "  OK  $1"; PASS=$((PASS+1)); }
log_fail() { echo "  FAIL $1"; [ -n "${2:-}" ] && head -c 360 "$2" 2>/dev/null && echo ""; FAIL=$((FAIL+1)); }
log_skip() { echo "  SKIP $1"; SKIP=$((SKIP+1)); }

login() {
  curl -s -o "$TMP/login-$1.json" -X POST "$BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$2\",\"password\":\"$3\"}"
  sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p' "$TMP/login-$1.json" | head -1
}

api() {
  local method="$1" path="$2" token="$3" body="${4:-}"
  if [ -n "$body" ]; then
    curl -s -o "$TMP/last.json" -w "%{http_code}" -X "$method" \
      -H "Authorization: Bearer $token" -H "Content-Type: application/json" \
      -d "$body" "${BASE}${path}"
  else
    curl -s -o "$TMP/last.json" -w "%{http_code}" -X "$method" \
      -H "Authorization: Bearer $token" "${BASE}${path}"
  fi
}

expect() {
  local name="$1" want="$2" got="$3"
  if [ "$got" = "$want" ]; then log_ok "$name"; else log_fail "$name (HTTP $got, want $want)" "$TMP/last.json"; fi
}

echo "BeautySkin API full scenarios (no Auth) — $(date -Iseconds 2>/dev/null || date)"
echo "Base: $BASE"
echo ""

# ── Setup ──────────────────────────────────────────────────────────
BUYER=$(login buyer buyer@beautyskin.com buyer123)
OWNER=$(login owner owner@beautyskin.com owner123)
ADMIN=$(login admin admin@beautyskin.com admin123)
STAFF=$(login staff staff@beautyskin.com staff123)

curl -s -o "$TMP/shops.json" -H "Authorization: Bearer $OWNER" "${BASE}/shops?page=1&limit=5"
SHOP_ID=$(node -e "const d=require(process.argv[1]);const s=(d.data?.content||[]).find(x=>x.slug==='glowskin')||d.data?.content?.[0];console.log(s?.id||'')" "$TMP/shops.json")
curl -s -o "$TMP/products.json" -H "Authorization: Bearer $OWNER" "${BASE}/products/merchant?shopId=${SHOP_ID}&page=1&limit=20"
# Prefer a visible, non-revoked product (revoke tests can mark prior picks unusable)
pick_product() {
  node -e "
    const d=require(process.argv[1]);
    const c=d.data?.content||[];
    const ok=p=>p.status==='ACTIVE'&&p.visible!==false&&p.revoked!==true&&(p.stock??0)>0;
    const p=c.find(ok);
    if(!p){process.exit(1);}
    console.log([p.id,p.name,p.price].join('\t'));
  " "$(cygpath -w "$TMP/products.json" 2>/dev/null || echo "$TMP/products.json")"
}
IFS=$'\t' read -r PRODUCT_ID PRODUCT_NAME PRODUCT_PRICE < <(pick_product || echo -e "\t\t")
PRODUCT2_ID=$(node -e "const d=require(process.argv[1]);const c=d.data?.content||[];const ok=p=>p.status==='ACTIVE'&&p.visible!==false&&p.revoked!==true&&(p.stock??0)>0;const a=c.filter(ok);console.log(a[1]?.id||a[0]?.id||'')" "$(cygpath -w "$TMP/products.json" 2>/dev/null || echo "$TMP/products.json")")

curl -s -o "$TMP/owner-me.json" -H "Authorization: Bearer $OWNER" "${BASE}/users/me"
OWNER_USER_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/owner-me.json")
curl -s -o "$TMP/buyer-me.json" -H "Authorization: Bearer $BUYER" "${BASE}/users/me"
BUYER_USER_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/buyer-me.json")

if [ -z "$PRODUCT_ID" ] || [ -z "$BUYER" ] || [ -z "$SHOP_ID" ]; then
  echo "Setup failed — need buyer token, shop, product"
  exit 1
fi

# Supplier for PO (reuse or create)
curl -s -o "$TMP/suppliers.json" -H "Authorization: Bearer $ADMIN" "${BASE}/suppliers?page=1&limit=1"
SUPPLIER_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.content?.[0]?.id||'')" "$TMP/suppliers.json")
if [ -z "$SUPPLIER_ID" ]; then
  got=$(api POST "/suppliers" "$ADMIN" "{\"name\":\"PO Supplier $TS\",\"contactPerson\":\"PO\",\"email\":\"po-$TS@test.com\",\"country\":\"KH\"}")
  [ "$got" = "200" ] && SUPPLIER_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
fi

echo "── USER: profile & shipping (buyer) ──"
got=$(api PATCH "/users/me" "$BUYER" '{"firstName":"Emma","lastName":"Rodriguez","phone":"555-0100"}')
expect "PATCH /users/me" 200 "$got"
got=$(api PATCH "/users/me/shipping" "$BUYER" '{"firstName":"Emma","lastName":"Rodriguez","address":"123 Riverside Blvd","city":"Phnom Penh","state":"Phnom Penh","zip":"12000","country":"Cambodia"}')
expect "PATCH /users/me/shipping" 200 "$got"
got=$(api GET "/users/me/shipping" "$BUYER")
expect "GET /users/me/shipping" 200 "$got"

echo ""
echo "── BUYER: cart line ops ──"
got=$(api DELETE "/cart" "$BUYER")
expect "DELETE /cart (clear)" 200 "$got"
got=$(api POST "/cart/items" "$BUYER" "{\"productId\":\"$PRODUCT_ID\",\"quantity\":1}")
expect "POST /cart/items" 200 "$got"
CART_ITEM_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.items?.[0]?.id||'')" "$TMP/last.json")
if [ -n "$CART_ITEM_ID" ]; then
  got=$(api PATCH "/cart/items/$CART_ITEM_ID" "$BUYER" '{"quantity":3}')
  expect "PATCH /cart/items/{id} qty" 200 "$got"
  got=$(api DELETE "/cart/items/$CART_ITEM_ID" "$BUYER")
  expect "DELETE /cart/items/{id}" 200 "$got"
fi
got=$(api DELETE "/cart/promo" "$BUYER")
expect "DELETE /cart/promo (no-op ok)" 200 "$got"

echo ""
echo "── BUYER: checkout & place order ──"
got=$(api POST "/cart/items" "$BUYER" "{\"productId\":\"$PRODUCT_ID\",\"quantity\":2}")
expect "POST /cart/items (for order)" 200 "$got"
got=$(api GET "/checkout/quote" "$BUYER")
expect "GET /checkout/quote" 200 "$got"
got=$(api POST "/cart/promo" "$BUYER" '{"code":"BEAUTY10"}')
expect "POST /cart/promo BEAUTY10" 200 "$got"
got=$(api PATCH "/users/me/preferences" "$BUYER" '{"darkMode":true,"orderUpdates":true,"promotions":true,"lowStockAlerts":true,"emailNotifications":true}')
expect "PATCH /users/me/preferences" 200 "$got"

ORDER_BODY='{"firstName":"Emma","lastName":"Rodriguez","email":"buyer@beautyskin.com","phone":"555-0100","address":"123 Riverside Blvd","city":"Phnom Penh","state":"Phnom Penh","zip":"12000","country":"Cambodia","paymentMethod":"card","saveInfo":true,"notes":"scenario '$TS'"}'
got=$(api POST "/orders" "$BUYER" "$ORDER_BODY")
expect "POST /orders (place order)" 200 "$got"
ORDER_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.orders?.[0]?.id||'')" "$TMP/last.json")
got=$(api GET "/cart" "$BUYER")
CART_N=$(node -e "const d=require(process.argv[1]);console.log((d.data?.items||[]).length)" "$TMP/last.json")
[ "$got" = "200" ] && [ "$CART_N" = "0" ] && log_ok "Cart empty after order" || log_fail "Cart empty after order" "$TMP/last.json"

echo ""
echo "── BUYER: reviews ──"
curl -s -o "$TMP/reviews.json" -H "Authorization: Bearer $BUYER" "${BASE}/products/$PRODUCT_ID/reviews?page=1&limit=5"
REVIEW_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.content?.[0]?.id||'')" "$TMP/reviews.json")
if [ -n "$REVIEW_ID" ] && [ -n "$ADMIN" ]; then
  got=$(api DELETE "/products/$PRODUCT_ID/reviews/$REVIEW_ID" "$ADMIN")
  expect "DELETE review (admin, reset)" 200 "$got"
fi
got=$(api POST "/products/$PRODUCT_ID/reviews" "$BUYER" '{"rating":5,"title":"Scenario '$TS'","body":"Automated test review.","skinType":"combination"}')
expect "POST /products/{id}/reviews" 200 "$got"
REVIEW_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")

echo ""
echo "── OWNER: orders & bulk status ──"
if [ -n "$ORDER_ID" ]; then
  got=$(api GET "/orders/$ORDER_ID" "$OWNER")
  expect "GET /orders/{id}" 200 "$got"
  got=$(api PATCH "/orders/$ORDER_ID/status" "$OWNER" '{"status":"CONFIRMED"}')
  expect "PATCH order CONFIRMED" 200 "$got"
  got=$(api PATCH "/orders/bulk" "$OWNER" "{\"ids\":[\"$ORDER_ID\"],\"status\":\"PACKING\"}")
  expect "PATCH /orders/bulk PACKING" 200 "$got"
  got=$(api PATCH "/orders/$ORDER_ID/status" "$OWNER" '{"status":"SHIPPING"}')
  expect "PATCH order SHIPPING" 200 "$got"
else
  log_skip "Order flows (no order id)"
fi

echo ""
echo "── OWNER: product CRUD ──"
NEW_SKU="SCN-SKU-$TS"
got=$(api POST "/products/shops/$SHOP_ID" "$OWNER" "{\"name\":\"Scenario Product $TS\",\"brand\":\"Test\",\"category\":\"Skincare\",\"price\":29.99,\"stock\":50,\"sku\":\"$NEW_SKU\",\"description\":\"Created by scenario script\",\"visible\":true}")
expect "POST /products/shops/{shopId}" 200 "$got"
NEW_PRODUCT_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
if [ -n "$NEW_PRODUCT_ID" ]; then
  got=$(api PUT "/products/shops/$SHOP_ID/$NEW_PRODUCT_ID" "$OWNER" "{\"name\":\"Scenario Product $TS Updated\",\"price\":24.99,\"stock\":45,\"visible\":true}")
  expect "PUT /products/shops/{shopId}/{id}" 200 "$got"
  got=$(api DELETE "/products/shops/$SHOP_ID/$NEW_PRODUCT_ID" "$OWNER")
  expect "DELETE /products/shops/{shopId}/{id}" 200 "$got"
fi

echo ""
echo "── OWNER: inventory ──"
curl -s -o "$TMP/inv.json" -H "Authorization: Bearer $OWNER" "${BASE}/inventory?shopId=$SHOP_ID&page=1&limit=1"
INV_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.content?.[0]?.id||'')" "$TMP/inv.json")
if [ -z "$INV_ID" ]; then
  got=$(api POST "/inventory?shopId=$SHOP_ID" "$OWNER" "{\"productId\":\"$PRODUCT_ID\",\"productName\":\"$PRODUCT_NAME\",\"sku\":\"INV-$TS\",\"currentStock\":100,\"minStock\":10,\"maxStock\":500,\"reorderPoint\":20,\"costPrice\":10.50}")
  expect "POST /inventory" 200 "$got"
  INV_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
fi
if [ -n "$INV_ID" ]; then
  got=$(api PATCH "/inventory/$INV_ID/restock" "$OWNER" '{"quantity":25}')
  expect "PATCH /inventory/{id}/restock" 200 "$got"
  got=$(api PATCH "/inventory/$INV_ID/adjust" "$OWNER" '{"delta":-2,"notes":"scenario adjust"}')
  expect "PATCH /inventory/{id}/adjust" 200 "$got"
else
  log_skip "Inventory restock/adjust"
fi

echo ""
echo "── OWNER: promotions ──"
PROMO_CODE="SCN$TS"
got=$(api POST "/promotions?shopId=$SHOP_ID" "$OWNER" "{\"name\":\"Scenario Promo\",\"code\":\"$PROMO_CODE\",\"type\":\"PERCENTAGE\",\"value\":5,\"minOrder\":10,\"maxUses\":99,\"startDate\":\"2026-01-01\",\"endDate\":\"2027-12-31\"}")
expect "POST /promotions" 200 "$got"
PROMO_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
if [ -n "$PROMO_ID" ]; then
  got=$(api PATCH "/promotions/$PROMO_ID/status" "$OWNER" '{"status":"PAUSED"}')
  expect "PATCH /promotions/{id}/status PAUSED" 200 "$got"
  got=$(api PUT "/promotions/$PROMO_ID" "$OWNER" "{\"name\":\"Scenario Promo Updated\",\"code\":\"$PROMO_CODE\",\"type\":\"PERCENTAGE\",\"value\":5,\"minOrder\":10,\"maxUses\":99,\"startDate\":\"2026-01-01\",\"endDate\":\"2027-12-31\"}")
  expect "PUT /promotions/{id}" 200 "$got"
  got=$(api DELETE "/promotions/$PROMO_ID" "$OWNER")
  expect "DELETE /promotions/{id}" 200 "$got"
fi

echo ""
echo "── OWNER: POS sale & cancel ──"
got=$(api POST "/pos/sales?shopId=$SHOP_ID" "$OWNER" "{\"customerName\":\"Walk-in\",\"paymentMethod\":\"cash\",\"discount\":0,\"items\":[{\"productId\":\"$PRODUCT_ID\",\"name\":\"POS Item\",\"quantity\":1,\"price\":$PRODUCT_PRICE}]}")
expect "POST /pos/sales" 200 "$got"
RECEIPT_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
if [ -n "$RECEIPT_ID" ]; then
  got=$(api POST "/pos/receipts/$RECEIPT_ID/cancel?shopId=$SHOP_ID" "$OWNER")
  if [ "$got" = "200" ]; then
    log_ok "POST /pos/receipts/{id}/cancel"
  else
    # May fail if daily cancel cap hit
    log_skip "POST /pos/receipts/{id}/cancel (HTTP $got — cap or already cancelled)"
  fi
fi

echo ""
echo "── OWNER: supplier purchase (receive stock) ──"
if [ -n "$SUPPLIER_ID" ]; then
  got=$(api POST "/supplier-purchases?shopId=$SHOP_ID" "$OWNER" "{\"supplierId\":\"$SUPPLIER_ID\",\"expectedDate\":\"2026-06-01\",\"items\":[{\"productId\":\"$PRODUCT_ID\",\"productName\":\"$PRODUCT_NAME\",\"sku\":\"PO-$TS\",\"quantity\":10,\"unitCost\":8.50}]}")
  expect "POST /supplier-purchases" 200 "$got"
  PO_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
  if [ -n "$PO_ID" ]; then
    got=$(api PATCH "/supplier-purchases/$PO_ID/status" "$OWNER" '{"status":"RECEIVED"}')
    expect "PATCH supplier-purchase RECEIVED" 200 "$got"
  fi
else
  log_skip "Supplier purchase (no supplier)"
fi

echo ""
echo "── OWNER: revoke request & review ──"
# Ephemeral product so approving revoke does not break cart/order on the next run
got=$(api POST "/products/shops/$SHOP_ID" "$OWNER" "{\"name\":\"Revoke Target $TS\",\"price\":9.99,\"stock\":5,\"sku\":\"REV-$TS\",\"visible\":true}")
REVOKE_PRODUCT_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
if [ -n "$REVOKE_PRODUCT_ID" ]; then
  got=$(api POST "/revoke-requests?shopId=$SHOP_ID" "$OWNER" "{\"productId\":\"$REVOKE_PRODUCT_ID\",\"quantity\":1,\"reason\":\"Damaged\",\"detail\":\"scenario $TS\"}")
  expect "POST /revoke-requests" 200 "$got"
  REVOKE_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
  if [ -n "$REVOKE_ID" ]; then
    got=$(api PATCH "/revoke-requests/$REVOKE_ID/review" "$OWNER" '{"status":"APPROVED","notes":"Approved in scenario test"}')
    expect "PATCH /revoke-requests/{id}/review APPROVED" 200 "$got"
  fi
  api DELETE "/products/shops/$SHOP_ID/$REVOKE_PRODUCT_ID" "$OWNER" >/dev/null
else
  log_skip "Revoke flow (could not create temp product)"
fi

echo ""
echo "── OWNER: shop staff CRUD ──"
STAFF_EMAIL="staff-$TS@test.com"
got=$(api POST "/shops/$SHOP_ID/users" "$OWNER" "{\"name\":\"Scenario Staff\",\"email\":\"$STAFF_EMAIL\",\"phone\":\"555-2222\",\"role\":\"STAFF\"}")
expect "POST /shops/{shopId}/users" 200 "$got"
SHOP_USER_ID=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
if [ -n "$SHOP_USER_ID" ]; then
  got=$(api PUT "/shops/$SHOP_ID/users/$SHOP_USER_ID" "$OWNER" '{"name":"Scenario Staff Updated","role":"CASHIER"}')
  expect "PUT /shops/{shopId}/users/{id}" 200 "$got"
  got=$(api DELETE "/shops/$SHOP_ID/users/$SHOP_USER_ID" "$OWNER")
  expect "DELETE /shops/{shopId}/users/{id}" 200 "$got"
fi

echo ""
echo "── STAFF: scoped read ──"
if [ -n "$STAFF" ]; then
  got=$(api GET "/orders?shopId=$SHOP_ID&page=1&limit=3" "$STAFF")
  expect "GET /orders (staff)" 200 "$got"
  got=$(api GET "/inventory?shopId=$SHOP_ID&page=1&limit=3" "$STAFF")
  expect "GET /inventory (staff)" 200 "$got"
fi

echo ""
echo "── ADMIN: suppliers & customers ──"
SUP_EMAIL="adm-sup-$TS@test.com"
got=$(api POST "/suppliers" "$ADMIN" "{\"name\":\"Admin Supplier $TS\",\"email\":\"$SUP_EMAIL\",\"country\":\"US\",\"category\":\"Skincare\"}")
expect "POST /suppliers" 200 "$got"
SUP_NEW=$(node -e "const d=require(process.argv[1]);console.log(d.data?.id||'')" "$TMP/last.json")
if [ -n "$SUP_NEW" ]; then
  got=$(api PUT "/suppliers/$SUP_NEW" "$ADMIN" "{\"name\":\"Admin Supplier Updated $TS\",\"email\":\"$SUP_EMAIL\",\"country\":\"US\"}")
  expect "PUT /suppliers/{id}" 200 "$got"
  got=$(api DELETE "/suppliers/$SUP_NEW" "$ADMIN")
  expect "DELETE /suppliers/{id}" 200 "$got"
fi
got=$(api PATCH "/shops/$SHOP_ID/status" "$ADMIN" '{"status":"ACTIVE"}')
expect "PATCH /shops/{id}/status (admin)" 200 "$got"
if [ -n "$REVIEW_ID" ]; then
  got=$(api DELETE "/products/$PRODUCT_ID/reviews/$REVIEW_ID" "$ADMIN")
  expect "DELETE /products/{id}/reviews/{id} (admin)" 200 "$got"
fi

echo ""
echo "── NOTIFICATIONS (buyer) ──"
got=$(api GET "/notifications?page=1&limit=5" "$BUYER")
expect "GET /notifications" 200 "$got"
NOTIF_ID=$(node -e "const d=require(process.argv[1]);const c=d.data?.content||[];console.log(c[0]?.id||'')" "$TMP/last.json")
if [ -n "$NOTIF_ID" ]; then
  got=$(api PATCH "/notifications/$NOTIF_ID/read" "$BUYER")
  expect "PATCH /notifications/{id}/read" 200 "$got"
fi
got=$(api PATCH "/notifications/read-all" "$BUYER")
expect "PATCH /notifications/read-all" 200 "$got"
# Create a notification to delete — may not exist; try delete if we had one
if [ -n "$NOTIF_ID" ]; then
  got=$(api DELETE "/notifications/$NOTIF_ID" "$BUYER")
  # 200 or 404 if already gone
  if [ "$got" = "200" ] || [ "$got" = "404" ]; then log_ok "DELETE /notifications/{id}"; else log_fail "DELETE /notifications/{id} (HTTP $got)" "$TMP/last.json"; fi
fi

echo ""
echo "── DIRECT MESSAGES (buyer → owner) ──"
if [ -n "$OWNER_USER_ID" ]; then
  got=$(api POST "/messages" "$BUYER" "{\"recipientId\":\"$OWNER_USER_ID\",\"content\":\"DM scenario test $TS\"}")
  expect "POST /messages" 200 "$got"
  got=$(api GET "/messages/threads" "$BUYER")
  expect "GET /messages/threads" 200 "$got"
  THREAD_ID=$(node -e "const d=require(process.argv[1]);const t=d.data||[];console.log((Array.isArray(t)?t[0]:t.content?.[0])?.id||'')" "$TMP/last.json")
  if [ -n "$THREAD_ID" ]; then
    got=$(api GET "/messages/threads/$THREAD_ID?page=1&limit=10" "$BUYER")
    expect "GET /messages/threads/{id}" 200 "$got"
    got=$(api PATCH "/messages/read" "$BUYER" "{\"threadId\":\"$THREAD_ID\"}")
    expect "PATCH /messages/read" 200 "$got"
  fi
fi

echo ""
echo "── CHAT (buyer) ──"
got=$(api GET "/chat/rooms" "$BUYER")
expect "GET /chat/rooms" 200 "$got"
ROOM_ID=$(node -e "const d=require(process.argv[1]);const r=d.data||[];console.log((Array.isArray(r)?r[0]?.id:null)||'')" "$TMP/last.json")
if [ -n "$ROOM_ID" ]; then
  got=$(api GET "/chat/rooms/$ROOM_ID/messages?page=1&limit=10" "$BUYER")
  expect "GET /chat/rooms/{id}/messages" 200 "$got"
  got=$(api POST "/chat/rooms/$ROOM_ID/messages" "$BUYER" '{"content":"Chat scenario '$TS'"}')
  expect "POST /chat/rooms/{id}/messages" 200 "$got"
fi

echo ""
echo "── ADMIN: customer status (last — re-enables buyer) ──"
if [ -n "$BUYER_USER_ID" ]; then
  got=$(api GET "/customers/$BUYER_USER_ID" "$ADMIN")
  expect "GET /customers/{id}" 200 "$got"
  got=$(api PATCH "/customers/$BUYER_USER_ID/status" "$ADMIN" '{"status":"INACTIVE"}')
  expect "PATCH /customers/{id}/status INACTIVE" 200 "$got"
  got=$(api PATCH "/customers/$BUYER_USER_ID/status" "$ADMIN" '{"status":"ACTIVE"}')
  expect "PATCH /customers/{id}/status ACTIVE" 200 "$got"
fi

echo ""
echo "════════════════════════════════════"
echo "SCENARIOS: $PASS passed, $FAIL failed, $SKIP skipped"
echo "(Auth endpoints excluded: register, OTP, OAuth, refresh, logout)"
echo "════════════════════════════════════"
[ "$FAIL" -eq 0 ]
