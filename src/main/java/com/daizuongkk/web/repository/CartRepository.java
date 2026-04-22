package com.daizuongkk.web.repository;

import com.daizuongkk.web.model.Cart;
import com.daizuongkk.web.model.CartItem;
import com.daizuongkk.web.model.ProductImg;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class CartRepository {



    public  List<CartItem> findItemsByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }

        String sql = "SELECT ci.* FROM carts c JOIN cart_items ci ON c.id = ci.cart_id  WHERE c.user_id = ? ORDER BY ci.created_at DESC";
        List<CartItem> carts = new ArrayList<>();

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    carts.add(resultSetToCartItem(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return carts;
    }



    private CartItem resultSetToCartItem(ResultSet resultSet) throws SQLException {
        return CartItem.builder()
                .id(resultSet.getLong("id"))
                .cartId(resultSet.getLong("cart_id"))
                .quantity(resultSet.getLong("quantity"))
                .productId(resultSet.getLong("product_id"))
                .build();
    }




    public Cart findByUserId(Long userId) {

        if (userId == null || userId <= 0) {
            return null;
        }

        String sql = "SELECT * FROM carts WHERE user_id = ?";
        Cart cart= new Cart();

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    cart.setId(resultSet.getLong("id"));
                    cart.setUserId(resultSet.getLong("user_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cart;
    }
}
