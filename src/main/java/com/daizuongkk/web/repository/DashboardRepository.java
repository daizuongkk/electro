package com.daizuongkk.web.repository;

import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.model.MonthlySalesStat;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepository {
    private static final String VALID_ORDER_STATUS = "('PAID','SHIPPED','COMPLETED')";

    public Long countOrders() {
        return queryLong("SELECT COUNT(*) FROM orders");
    }

    public Double sumValidOrderRevenue() {
        return queryDouble("SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE status IN " + VALID_ORDER_STATUS);
    }

    public Long countOrdersByStatus(String status) {
        if (status == null || status.isBlank()) {
            return 0L;
        }

        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.trim().toUpperCase());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public List<Order> findRecentOrders(int limit) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, user_id, recipient_name, phone, total_price, status, address, created_at FROM orders ORDER BY created_at DESC LIMIT ?";

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(limit, 1));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(Order.builder()
                            .id(resultSet.getLong("id"))
                            .userId(resultSet.getLong("user_id"))
                            .recipientName(resultSet.getString("recipient_name"))
                            .phone(resultSet.getString("phone"))
                            .totalPrice(resultSet.getDouble("total_price"))
                            .status(resultSet.getString("status"))
                            .address(resultSet.getString("address"))
                            .createdAt(resultSet.getTimestamp("created_at"))
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public YearMonth findLatestOrderMonth() {
        String sql = "SELECT MAX(created_at) FROM orders";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next() && resultSet.getTimestamp(1) != null) {
                return YearMonth.from(resultSet.getTimestamp(1).toLocalDateTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return YearMonth.now();
    }

    public List<MonthlySalesStat> findMonthlySales(YearMonth startMonth, YearMonth endMonth) {
        List<MonthlySalesStat> result = new ArrayList<>();
        String sql = """
                SELECT YEAR(created_at) AS order_year,
                       MONTH(created_at) AS order_month,
                       COALESCE(SUM(total_price), 0) AS revenue,
                       COUNT(*) AS order_count
                FROM orders
                WHERE status IN ('PAID','SHIPPED','COMPLETED')
                  AND created_at >= ?
                  AND created_at < ?
                GROUP BY YEAR(created_at), MONTH(created_at)
                ORDER BY order_year, order_month
                """;

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(startMonth.atDay(1).atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(endMonth.plusMonths(1).atDay(1).atStartOfDay()));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    YearMonth month = YearMonth.of(resultSet.getInt("order_year"), resultSet.getInt("order_month"));
                    result.add(MonthlySalesStat.builder()
                            .month(month)
                            .revenue(resultSet.getDouble("revenue"))
                            .orderCount(resultSet.getLong("order_count"))
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Long countFirstTimeCustomers() {
        return queryLong("""
                SELECT COUNT(*)
                FROM (
                    SELECT user_id
                    FROM orders
                    WHERE status IN ('PAID','SHIPPED','COMPLETED')
                    GROUP BY user_id
                    HAVING COUNT(*) = 1
                ) customer_orders
                """);
    }

    public Long countReturningCustomers() {
        return queryLong("""
                SELECT COUNT(*)
                FROM (
                    SELECT user_id
                    FROM orders
                    WHERE status IN ('PAID','SHIPPED','COMPLETED')
                    GROUP BY user_id
                    HAVING COUNT(*) > 1
                ) customer_orders
                """);
    }

    public Long countValidOrders() {
        return queryLong("SELECT COUNT(*) FROM orders WHERE status IN " + VALID_ORDER_STATUS);
    }

    public Long countActiveProducts() {
        return queryLong("SELECT COUNT(*) FROM products WHERE deleted = 0");
    }

    private Long queryLong(String sql) {
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private Double queryDouble(String sql) {
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0D;
    }
}
