package com.daizuongkk.web.repository;

import com.daizuongkk.web.dto.request.AdminReportFilterRequest;
import com.daizuongkk.web.dto.response.InventoryReportRowResponse;
import com.daizuongkk.web.dto.response.OrderReportRowResponse;
import com.daizuongkk.web.dto.response.ProductSalesReportRowResponse;
import com.daizuongkk.web.dto.response.RevenueReportRowResponse;
import com.daizuongkk.web.util.JDBCUtils;
import com.daizuongkk.web.util.ReportFormatUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    public List<RevenueReportRowResponse> findRevenueRows(AdminReportFilterRequest filters) {
        StringBuilder sql = new StringBuilder("""
                SELECT DATE(o.created_at) AS report_date,
                       COUNT(*) AS order_count,
                       COALESCE(SUM(order_quantity.product_count), 0) AS product_count,
                       COALESCE(SUM(o.total_price), 0) AS revenue
                FROM orders o
                LEFT JOIN (
                    SELECT order_id, SUM(quantity) AS product_count
                    FROM order_items
                    GROUP BY order_id
                ) order_quantity ON order_quantity.order_id = o.id
                WHERE 1=1
                """);
        List<Object> params = appendOrderFilters(sql, filters);
        sql.append(" GROUP BY DATE(o.created_at) ORDER BY report_date ASC");

        List<RevenueReportRowResponse> rows = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Date reportDate = resultSet.getDate("report_date");
                    rows.add(RevenueReportRowResponse.builder()
                            .reportDate(reportDate == null ? null : reportDate.toLocalDate())
                            .orderCount(resultSet.getLong("order_count"))
                            .productCount(resultSet.getLong("product_count"))
                            .revenue(resultSet.getDouble("revenue"))
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<OrderReportRowResponse> findOrderRows(AdminReportFilterRequest filters, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT o.id,
                       o.created_at,
                       o.total_price,
                       o.status,
                       o.address,
                       o.recipient_name,
                       u.username,
                       u.first_name,
                       u.last_name
                FROM orders o
                LEFT JOIN users u ON u.id = o.user_id
                WHERE 1=1
                """);
        List<Object> params = appendOrderFilters(sql, filters);
        sql.append(" ORDER BY o.created_at DESC, o.id DESC LIMIT ?");
        params.add(Math.max(limit, 1));

        List<OrderReportRowResponse> rows = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String status = resultSet.getString("status");
                    rows.add(OrderReportRowResponse.builder()
                            .orderId(resultSet.getLong("id"))
                            .customerName(buildCustomerName(resultSet))
                            .createdAt(resultSet.getTimestamp("created_at"))
                            .totalAmount(resultSet.getDouble("total_price"))
                            .status(status)
                            .statusLabel(ReportFormatUtils.statusLabel(status))
                            .paymentMethod(ReportFormatUtils.paymentMethodFromAddress(resultSet.getString("address")))
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<ProductSalesReportRowResponse> findTopSellingProducts(AdminReportFilterRequest filters, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id AS product_id,
                       p.name AS product_name,
                       p.category,
                       COALESCE(SUM(oi.quantity), 0) AS quantity_sold,
                       COALESCE(SUM(oi.price * oi.quantity), 0) AS revenue
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                WHERE 1=1
                """);
        List<Object> params = appendOrderFilters(sql, filters);
        sql.append("""
                GROUP BY p.id, p.name, p.category
                ORDER BY quantity_sold DESC, revenue DESC, p.id ASC
                LIMIT ?
                """);
        params.add(Math.max(limit, 1));

        List<ProductSalesReportRowResponse> rows = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(ProductSalesReportRowResponse.builder()
                            .productId(resultSet.getLong("product_id"))
                            .productName(resultSet.getString("product_name"))
                            .category(resultSet.getString("category"))
                            .quantitySold(resultSet.getLong("quantity_sold"))
                            .revenue(resultSet.getDouble("revenue"))
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<InventoryReportRowResponse> findInventoryRows() {
        String sql = """
                SELECT p.id, p.name, p.category, p.quantity, p.price
                FROM products p
                ORDER BY CASE
                    WHEN COALESCE(p.quantity, 0) = 0 THEN 0
                    WHEN COALESCE(p.quantity, 0) <= 10 THEN 1
                    ELSE 2
                END, COALESCE(p.quantity, 0) ASC, p.name ASC
                """;

        List<InventoryReportRowResponse> rows = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Long quantity = resultSet.getLong("quantity");
                rows.add(InventoryReportRowResponse.builder()
                        .productId(resultSet.getLong("id"))
                        .productName(resultSet.getString("name"))
                        .category(resultSet.getString("category"))
                        .quantity(quantity)
                        .price(resultSet.getDouble("price"))
                        .stockStatus(ReportFormatUtils.stockStatus(quantity))
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private List<Object> appendOrderFilters(StringBuilder sql, AdminReportFilterRequest filters) {
        List<Object> params = new ArrayList<>();
        if (filters == null) {
            return params;
        }

        LocalDate fromDate = filters.getFromDate();
        if (fromDate != null) {
            sql.append(" AND o.created_at >= ?");
            params.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }

        LocalDate toDate = filters.getToDate();
        if (toDate != null) {
            sql.append(" AND o.created_at <= ?");
            params.add(Timestamp.valueOf(toDate.atTime(LocalTime.MAX)));
        }

        String status = filters.getStatus();
        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status = ?");
            params.add(status.trim().toUpperCase());
        }
        return params;
    }

    private void bind(PreparedStatement statement, List<Object> params) throws Exception {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private String buildCustomerName(ResultSet resultSet) throws Exception {
        String firstName = valueOrEmpty(resultSet.getString("first_name"));
        String lastName = valueOrEmpty(resultSet.getString("last_name"));
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }

        String recipientName = resultSet.getString("recipient_name");
        if (recipientName != null && !recipientName.isBlank()) {
            return recipientName;
        }

        String username = resultSet.getString("username");
        return username == null || username.isBlank() ? "Khách hàng" : username;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
