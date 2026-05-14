package com.daizuongkk.web.repository;

import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.model.OrderItem;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class OrderRepository {

    public Order create(Order order, List<OrderItem> items) {
        if (order == null || order.getUserId() == null || order.getUserId() <= 0 || items == null || items.isEmpty()) {
            return null;
        }

        String orderSql = "INSERT INTO orders (user_id, total_price, status, recipient_name, phone, address) VALUES (?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, product_id, price, quantity) VALUES (?, ?, ?, ?)";
        String clearCartSql = buildSelectedCartDeleteSql(items.size());

        Connection connection = null;
        try {
            connection = JDBCUtils.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement orderStatement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStatement.setLong(1, order.getUserId());
                orderStatement.setDouble(2, order.getTotalPrice());
                orderStatement.setString(3, order.getStatus());
                orderStatement.setString(4, order.getRecipientName());
                orderStatement.setString(5, order.getPhone());
                orderStatement.setString(6, order.getAddress());

                if (orderStatement.executeUpdate() == 0) {
                    connection.rollback();
                    return null;
                }

                try (ResultSet generatedKeys = orderStatement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        connection.rollback();
                        return null;
                    }
                    order.setId(generatedKeys.getLong(1));
                }
            }

            try (PreparedStatement itemStatement = connection.prepareStatement(itemSql)) {
                for (OrderItem item : items) {
                    itemStatement.setLong(1, order.getId());
                    itemStatement.setLong(2, item.getProductId());
                    itemStatement.setDouble(3, item.getPrice());
                    itemStatement.setLong(4, item.getQuantity());
                    itemStatement.addBatch();
                }
                itemStatement.executeBatch();
            }

            try (PreparedStatement clearCartStatement = connection.prepareStatement(clearCartSql)) {
                clearCartStatement.setLong(1, order.getUserId());
                for (int i = 0; i < items.size(); i++) {
                    clearCartStatement.setLong(i + 2, items.get(i).getProductId());
                }
                clearCartStatement.executeUpdate();
            }

            connection.commit();
            return order;
        } catch (Exception e) {
            rollback(connection);
            e.printStackTrace();
            return null;
        } finally {
            close(connection);
        }
    }

    public List<Order> findByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        List<Order> orders = new ArrayList<>();

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Order order = resultSetToOrder(resultSet);
                    order.setItems(findItemsByOrderId(connection, order.getId()));
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY created_at DESC, id DESC";
        List<Order> orders = new ArrayList<>();

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Order order = resultSetToOrder(resultSet);
                order.setItems(findItemsByOrderId(connection, order.getId()));
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public long countByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return 0L;
        }

        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
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

    public boolean updateStatus(Long orderId, String status) {
        if (orderId == null || orderId <= 0 || status == null || status.isBlank()) {
            return false;
        }

        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.trim().toUpperCase());
            statement.setLong(2, orderId);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order findByIdAndUserId(Long orderId, Long userId) {
        if (orderId == null || orderId <= 0 || userId == null || userId <= 0) {
            return null;
        }

        String sql = "SELECT * FROM orders WHERE id = ? AND user_id = ? LIMIT 1";

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Order order = resultSetToOrder(resultSet);
                    order.setItems(findItemsByOrderId(connection, order.getId()));
                    return order;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<OrderItem> findItemsByOrderId(Connection connection, Long orderId) throws Exception {
        String sql = """
                SELECT oi.*, p.name AS product_name,
                       (SELECT pi.image_url FROM product_images pi WHERE pi.product_id = oi.product_id ORDER BY pi.id ASC LIMIT 1) AS product_image_url
                FROM order_items oi
                JOIN products p ON p.id = oi.product_id
                WHERE oi.order_id = ?
                ORDER BY oi.id ASC
                """;
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(resultSetToOrderItem(resultSet));
                }
            }
        }

        return items;
    }

    private String buildSelectedCartDeleteSql(int itemCount) {
        StringJoiner placeholders = new StringJoiner(",");
        for (int i = 0; i < itemCount; i++) {
            placeholders.add("?");
        }
        return "DELETE ci FROM cart_items ci JOIN carts c ON c.id = ci.cart_id WHERE c.user_id = ? AND ci.product_id IN ("
                + placeholders + ")";
    }

    private Order resultSetToOrder(ResultSet resultSet) throws Exception {
        return Order.builder()
                .id(resultSet.getLong("id"))
                .userId(resultSet.getLong("user_id"))
                .totalPrice(resultSet.getDouble("total_price"))
                .status(resultSet.getString("status"))
                .recipientName(resultSet.getString("recipient_name"))
                .phone(resultSet.getString("phone"))
                .address(resultSet.getString("address"))
                .createdAt(resultSet.getTimestamp("created_at"))
                .build();
    }

    private OrderItem resultSetToOrderItem(ResultSet resultSet) throws Exception {
        return OrderItem.builder()
                .id(resultSet.getLong("id"))
                .orderId(resultSet.getLong("order_id"))
                .productId(resultSet.getLong("product_id"))
                .price(resultSet.getDouble("price"))
                .quantity(resultSet.getLong("quantity"))
                .productName(resultSet.getString("product_name"))
                .productImageUrl(resultSet.getString("product_image_url"))
                .build();
    }

    private void rollback(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
