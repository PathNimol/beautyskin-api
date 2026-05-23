package com.acleda.bsonlineshop.service.impl;

import com.acleda.bsonlineshop.entity.InventoryItem;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.enums.InventoryStatus;
import com.acleda.bsonlineshop.enums.OrderStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.mapper.OrderMapper;
import com.acleda.bsonlineshop.repository.InventoryItemRepository;
import com.acleda.bsonlineshop.repository.OrderRepository;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import com.acleda.bsonlineshop.security.SecurityUtils;
import com.acleda.bsonlineshop.service.DashboardService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final ZoneId DASHBOARD_ZONE = ZoneOffset.UTC;
    private static final List<InventoryStatus> LOW_STOCK_STATUSES =
            List.of(InventoryStatus.LOW, InventoryStatus.CRITICAL, InventoryStatus.OUT_OF_STOCK);

    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> adminDashboard() {
        Map<String, Object> data = new HashMap<>();
        Instant to = Instant.now();
        Instant from30 = to.minus(30, ChronoUnit.DAYS);

        data.put("totalShops", shopRepository.count());
        data.put("totalOrders", orderRepository.count());
        data.put("totalProducts", productRepository.count());
        data.put("totalCustomers", userRepository.countByRoleAndDeletedFalse(UserRole.CUSTOMER));
        data.put(
                "totalStaff",
                userRepository.countByRoleAndDeletedFalse(UserRole.STAFF)
                        + userRepository.countByRoleAndDeletedFalse(UserRole.OWNER));
        data.put("revenue30d", orderRepository.sumDeliveredRevenue(null, from30, to));
        data.put(
                "recentOrders",
                orderRepository.searchScoped(null, null, null, null, PageRequest.of(0, 5)).getContent().stream()
                        .map(orderMapper::toResponse)
                        .toList());
        data.put(
                "pendingOrders",
                orderRepository
                        .searchScoped(null, null, OrderStatus.PENDING, null, PageRequest.of(0, 1))
                        .getTotalElements());
        data.put("revenueByMonth", buildRevenueByMonth());
        data.put("salesByDay", buildSalesByDay());
        data.put("categoryBreakdown", buildCategoryBreakdown());
        data.put("lowStockAlerts", buildLowStockAlerts());
        data.put("expiredProducts", buildExpiredProducts());
        data.put("topProducts", buildTopProducts());
        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> shopDashboard(UUID shopId) {
        UUID scoped = SecurityUtils.requireShopId(shopId);
        Map<String, Object> data = new HashMap<>();
        data.put("shopId", scoped);
        data.put(
                "orders",
                orderRepository.searchScoped(null, scoped, null, null, PageRequest.of(0, 1)).getTotalElements());
        data.put("products", countShopProducts(scoped));
        data.put(
                "lowStockItems",
                inventoryItemRepository.search(scoped, InventoryStatus.LOW, null, PageRequest.of(0, 5)).getContent());
        Instant to = Instant.now();
        Instant from = to.minus(30, ChronoUnit.DAYS);
        data.put("revenue", orderRepository.sumDeliveredRevenue(scoped, from, to));
        data.put(
                "recentOrders",
                orderRepository.searchScoped(null, scoped, null, null, PageRequest.of(0, 5)).getContent().stream()
                        .map(orderMapper::toResponse)
                        .toList());
        return data;
    }

    private List<Map<String, Object>> buildRevenueByMonth() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.now(DASHBOARD_ZONE).minusMonths(i);
            Instant from = ym.atDay(1).atStartOfDay(DASHBOARD_ZONE).toInstant();
            Instant toEx = ym.plusMonths(1).atDay(1).atStartOfDay(DASHBOARD_ZONE).toInstant();
            BigDecimal revenue = orderRepository.sumDeliveredRevenue(null, from, toEx);
            long orders = orderRepository.countDeliveredInRange(null, from, toEx);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            row.put("revenue", revenue);
            row.put("orders", orders);
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildSalesByDay() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now(DASHBOARD_ZONE).minusDays(i);
            Instant from = day.atStartOfDay(DASHBOARD_ZONE).toInstant();
            Instant toEx = day.plusDays(1).atStartOfDay(DASHBOARD_ZONE).toInstant();
            BigDecimal sales = orderRepository.sumDeliveredRevenue(null, from, toEx);
            long returns = orderRepository.countCancelledInRange(null, from, toEx);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("day", day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            row.put("sales", sales);
            row.put("returns", returns);
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildCategoryBreakdown() {
        List<Object[]> raw = productRepository.sumSoldByCategory();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object[] row : raw) {
            String category = row[0] != null ? row[0].toString() : "Other";
            long sold = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            if (sold <= 0) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", category);
            entry.put("value", sold);
            rows.add(entry);
        }
        return rows;
    }

    private List<Map<String, Object>> buildLowStockAlerts() {
        return inventoryItemRepository
                .findPlatformByStatuses(LOW_STOCK_STATUSES, PageRequest.of(0, 10))
                .getContent()
                .stream()
                .map(this::toLowStockAlert)
                .toList();
    }

    private Map<String, Object> toLowStockAlert(InventoryItem item) {
        String severity =
                item.getInvStatus() == InventoryStatus.CRITICAL
                                || item.getInvStatus() == InventoryStatus.OUT_OF_STOCK
                        ? "critical"
                        : "warning";
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", item.getProductName());
        row.put("stock", item.getCurrentStock());
        row.put("threshold", item.getReorderPoint() > 0 ? item.getReorderPoint() : item.getMinStock());
        row.put("severity", severity);
        row.put("shopName", item.getShop() != null ? item.getShop().getName() : null);
        return row;
    }

    private List<Map<String, Object>> buildExpiredProducts() {
        return productRepository.findExpiredBefore(LocalDate.now(DASHBOARD_ZONE), PageRequest.of(0, 10)).getContent().stream()
                .map(this::toExpiredProductRow)
                .toList();
    }

    private Map<String, Object> toExpiredProductRow(Product p) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", p.getName());
        row.put("batch", p.getSku() != null ? p.getSku() : "—");
        row.put(
                "expiredOn",
                p.getExpiryDate() != null
                        ? p.getExpiryDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        : "—");
        row.put("qty", p.getStock());
        row.put("shop", p.getShop() != null ? p.getShop().getName() : "—");
        return row;
    }

    private List<Map<String, Object>> buildTopProducts() {
        List<Product> products = productRepository.findTopBySold(PageRequest.of(0, 5)).getContent();
        if (products.isEmpty()) {
            return List.of();
        }
        int maxSold = products.stream().mapToInt(Product::getSold).max().orElse(1);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Product p : products) {
            BigDecimal revenue =
                    p.getPrice().multiply(BigDecimal.valueOf(Math.max(p.getSold(), 0)));
            int progress = maxSold > 0 ? (int) Math.round((p.getSold() * 100.0) / maxSold) : 0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", p.getName());
            row.put("brand", p.getBrand() != null ? p.getBrand() : "");
            row.put("sold", p.getSold());
            row.put("revenue", revenue.setScale(2, RoundingMode.HALF_UP));
            row.put("progress", progress);
            rows.add(row);
        }
        return rows;
    }

    private long countShopProducts(UUID shopId) {
        Specification<Product> spec = (root, query, cb) ->
                cb.and(cb.isFalse(root.get("deleted")), cb.equal(root.get("shop").get("id"), shopId));
        return productRepository.count(spec);
    }
}
