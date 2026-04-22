package com.daizuongkk.web.repository;

import com.daizuongkk.web.model.Cart;
import com.daizuongkk.web.model.CartItem;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CartItemRepository {




    public CartItem save(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        String sql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, cartItem.getCartId());
            statement.setLong(2, cartItem.getProductId());
            statement.setLong(3, cartItem.getQuantity());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Creating cart item failed, no rows affected.");
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cartItem.setId(generatedKeys.getLong(1));
                } else {
                    throw new RuntimeException("Creating cart item failed, no ID obtained.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return cartItem;
    }

    public CartItem update(CartItem cartItem) {

        if (cartItem == null) {
            return null;
        }

        Long cartId = cartItem.getCartId();

        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cartItem.getQuantity());
            statement.setLong(2, cartItem.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Updating cart item failed, no rows affected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return cartItem;
    }

    public void delete(String productIds, Long userId) {
        if (productIds == null || productIds.trim().isEmpty() || userId == null || userId <= 0) {
            return;
        }

        String sql = "DELETE ci FROM cart_items ci JOIN carts c ON c.id = ci.cart_id WHERE c.user_id = ? AND ci.product_id IN (" + productIds + ")";

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
