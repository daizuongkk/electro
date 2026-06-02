package com.daizuongkk.web.repository;

import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.model.MonthlySalesStat;
import com.daizuongkk.web.dto.response.OrderStatusReportResponse;
import com.daizuongkk.web.dto.response.SalesTrendResponse;
import com.daizuongkk.web.dto.response.TopProductReportResponse;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
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

    public Long countOrdersBetween(LocalDate fromDate, LocalDate toDate) {
        return queryLongBetween("SELECT COUNT(*) FROM orders WHERE created_at >= ? AND created_at <= ?", fromDate, toDate);
    }

    public Long countValidOrdersBetween(LocalDate fromDate, LocalDate toDate) {
        return queryLongBetween("SELECT COUNT(*) FROM orders WHERE status IN " + VALID_ORDER_STATUS + " AND created_at >= ? AND created_at <= ?",
                fromDate, toDate);
    }

    public Double sumValidOrderRevenueBetween(LocalDate fromDate, LocalDate toDate) {
        return queryDoubleBetween("SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE status IN " + VALID_ORDER_STATUS + " AND created_at >= ? AND created_at <= ?",
                fromDate, toDate);
    }

    public Long countOrdersByStatusBetween(String status, LocalDate fromDate, LocalDate toDate) {
        if (status == null || status.isBlank()) {
            return 0L;
        }

        String sql = "SELECT COUNT(*) FROM orders WHERE status = ? AND created_at >= ? AND created_at <= ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.trim().toUpperCase());
            setDateRange(statement, 2, fromDate, toDate);
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

    public Long countUniqueBuyingCustomersBetween(LocalDate fromDate, LocalDate toDate) {
        return queryLongBetween("""
                SELECT COUNT(DISTINCT user_id)
                FROM orders
                WHERE status IN ('PAID','SHIPPED','COMPLETED')
                  AND created_at >= ?
                  AND created_at <= ?
                """, fromDate, toDate);
    }

    public Long sumItemsSoldBetween(LocalDate fromDate, LocalDate toDate) {
        return queryLongBetween("""
                SELECT COALESCE(SUM(oi.quantity), 0)
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                WHERE o.status IN ('PAID','SHIPPED','COMPLETED')
                  AND o.created_at >= ?
                  AND o.created_at <= ?
                """, fromDate, toDate);
    }

    public List<SalesTrendResponse> findSalesTrend(LocalDate fromDate, LocalDate toDate) {
        List<SalesTrendResponse> result = new ArrayList<>();
        boolean groupByMonth = fromDate != null && toDate != null && fromDate.plusDays(62).isBefore(toDate);
        String labelExpression = groupByMonth ? "DATE_FORMAT(created_at, '%m/%Y')" : "DATE_FORMAT(created_at, '%d/%m')";
        String orderExpression = groupByMonth ? "YEAR(created_at), MONTH(created_at)" : "DATE(created_at)";
        String sql = """
                SELECT %s AS label,
                       COALESCE(SUM(total_price), 0) AS revenue,
                       COUNT(*) AS order_count
                FROM orders
                WHERE status IN ('PAID','SHIPPED','COMPLETED')
                  AND created_at >= ?
                  AND created_at <= ?
                GROUP BY %s
                ORDER BY %s
                """.formatted(labelExpression, orderExpression, orderExpression);

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setDateRange(statement, 1, fromDate, toDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(SalesTrendResponse.builder()
                            .label(resultSet.getString("label"))
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

    public List<OrderStatusReportResponse> findStatusBreakdown(LocalDate fromDate, LocalDate toDate) {
        List<OrderStatusReportResponse> result = new ArrayList<>();
        String sql = """
                SELECT status,
                       COUNT(*) AS order_count,
                       COALESCE(SUM(total_price), 0) AS revenue
                FROM orders
                WHERE created_at >= ?
                  AND created_at <= ?
                GROUP BY status
                ORDER BY FIELD(status, 'PENDING', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED')
                """;

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setDateRange(statement, 1, fromDate, toDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(OrderStatusReportResponse.builder()
                            .status(resultSet.getString("status"))
                            .orderCount(resultSet.getLong("order_count"))
                            .revenue(resultSet.getDouble("revenue"))
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<TopProductReportResponse> findTopSellingProducts(LocalDate fromDate, LocalDate toDate, int limit) {
        List<TopProductReportResponse> result = new ArrayList<>();
        String sql = """
                SELECT p.id AS product_id,
                       p.name AS product_name,
                       COALESCE(SUM(oi.quantity), 0) AS quantity_sold,
                       COALESCE(SUM(oi.price * oi.quantity), 0) AS revenue
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                WHERE o.status IN ('PAID','SHIPPED','COMPLETED')
                  AND o.created_at >= ?
                  AND o.created_at <= ?
                GROUP BY p.id, p.name
                ORDER BY revenue DESC, quantity_sold DESC
                LIMIT ?
                """;

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setDateRange(statement, 1, fromDate, toDate);
            statement.setInt(3, Math.max(limit, 1));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(TopProductReportResponse.builder()
                            .productId(resultSet.getLong("product_id"))
                            .productName(resultSet.getString("product_name"))
                            .quantitySold(resultSet.getLong("quantity_sold"))
                            .revenue(resultSet.getDouble("revenue"))
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Order> findRecentOrdersBetween(LocalDate fromDate, LocalDate toDate, int limit) {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT id, user_id, recipient_name, phone, total_price, status, address, created_at
                FROM orders
                WHERE created_at >= ?
                  AND created_at <= ?
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """;

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setDateRange(statement, 1, fromDate, toDate);
            statement.setInt(3, Math.max(limit, 1));
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

    private Long queryLongBetween(String sql, LocalDate fromDate, LocalDate toDate) {
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setDateRange(statement, 1, fromDate, toDate);
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

    private Double queryDoubleBetween(String sql, LocalDate fromDate, LocalDate toDate) {
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setDateRange(statement, 1, fromDate, toDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0D;
    }

    private void setDateRange(PreparedStatement statement, int startIndex, LocalDate fromDate, LocalDate toDate) throws Exception {
        LocalDate safeFromDate = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
        LocalDate safeToDate = toDate == null ? LocalDate.now() : toDate;
        statement.setTimestamp(startIndex, Timestamp.valueOf(safeFromDate.atStartOfDay()));
        statement.setTimestamp(startIndex + 1, Timestamp.valueOf(safeToDate.atTime(LocalTime.MAX)));
    }
}
