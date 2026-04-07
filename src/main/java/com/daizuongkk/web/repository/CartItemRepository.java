package com.daizuongkk.web.repository;

import com.daizuongkk.web.model.Cart;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CartItemRepository {


    public Cart findByUserId(Long userId) {

        if (userId == null || userId <= 0) {
            return null;
        }

        String sql = "SELECT ci.* FROM cart_items ci JOIN carts c ON c.id = ci.cart_id WHERE user_id = ?";
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
