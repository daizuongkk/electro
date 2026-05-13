package com.daizuongkk.web.repository;

import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WishlistRepository {
	private static final Logger LOGGER = Logger.getLogger(WishlistRepository.class.getName());

	public List<Long> findProductIdsByUserId(Long userId) {
		if (userId == null || userId <= 0) {
			return Collections.emptyList();
		}

		String sql = "SELECT product_id FROM wishlists WHERE user_id = ? ORDER BY product_id DESC";
		List<Long> productIds = new ArrayList<>();

		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					productIds.add(resultSet.getLong("product_id"));
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to load wishlist product ids", e);
		}

		return productIds;
	}

	public boolean exists(Long userId, Long productId) {
		if (userId == null || userId <= 0 || productId == null || productId <= 0) {
			return false;
		}

		String sql = "SELECT 1 FROM wishlists WHERE user_id = ? AND product_id = ? LIMIT 1";

		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, userId);
			statement.setLong(2, productId);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to check wishlist existence", e);
		}

		return false;
	}

	public boolean save(Long userId, Long productId) {
		if (userId == null || userId <= 0 || productId == null || productId <= 0) {
			return false;
		}

		if (exists(userId, productId)) {
			return true;
		}

		String sql = "INSERT INTO wishlists (user_id, product_id) VALUES (?, ?)";

		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, userId);
			statement.setLong(2, productId);
			return statement.executeUpdate() > 0;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to save wishlist item", e);
		}

		return false;
	}

	public boolean delete(Long userId, Long productId) {
		if (userId == null || userId <= 0 || productId == null || productId <= 0) {
			return false;
		}

		String sql = "DELETE FROM wishlists WHERE user_id = ? AND product_id = ?";

		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, userId);
			statement.setLong(2, productId);
			return statement.executeUpdate() > 0;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to delete wishlist item", e);
		}

		return false;
	}
}

