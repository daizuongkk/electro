package com.daizuongkk.web.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.daizuongkk.web.model.Cart;
import com.daizuongkk.web.model.CartItem;
import com.daizuongkk.web.util.JDBCUtils;

public class CartRepository {

	public List<CartItem> findItemsByUserId(Long userId) {
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

		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					Cart cart = new Cart();
					cart.setId(resultSet.getLong("id"));
					cart.setUserId(resultSet.getLong("user_id"));
					return cart;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Cart createForUser(Long userId) {
		if (userId == null || userId <= 0) {
			return null;
		}

		String sql = "INSERT INTO carts (user_id) VALUES (?)";

		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setLong(1, userId);

			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				return null;
			}

			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					Cart cart = new Cart();
					cart.setId(generatedKeys.getLong(1));
					cart.setUserId(userId);
					return cart;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Cart findOrCreateByUserId(Long userId) {
		Cart cart = findByUserId(userId);
		if (cart != null) {
			return cart;
		}

		return createForUser(userId);
	}
}
