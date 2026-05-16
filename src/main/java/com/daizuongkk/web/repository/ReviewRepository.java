package com.daizuongkk.web.repository;

import com.daizuongkk.web.dto.request.AdminReviewSearchRequest;
import com.daizuongkk.web.model.Review;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReviewRepository {


	public Long countByScore(Long productId, Integer score) {
		Long count = 0L;

		if (productId == null ||  score == null) {
			return count;
		}

		String sql = "select count(*) from reviews where product_id = ?  and score = ?";
		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setLong(1, productId);
			statement.setInt(2, score);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					count = resultSet.getLong(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}


	public List<Review> findByProductId(Long productId, int page, int size) {
		if (productId == null || productId <= 0) {
			return Collections.emptyList();
		}

		int normalizedPage = Math.max(page, 1);
		int normalizedSize = Math.max(size, 1);
		int offset = (normalizedPage - 1) * normalizedSize;

		String sql = "SELECT * FROM reviews " +
				"WHERE product_id = ? " +
				"ORDER BY created_at DESC " +
				"LIMIT ? OFFSET ?";

		List<Review> reviews = new ArrayList<>();

		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setLong(1, productId);
			statement.setInt(2, normalizedSize);
			statement.setInt(3, offset);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					reviews.add(resultSetToReview(resultSet));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reviews;
	}

	public  boolean create(Long productId, Long userId, String message, int score) {
		if (productId == null || productId <= 0 || userId == null || userId <= 0) {
			return false;
		}

		String normalizedMessage = message == null ? "" : message.trim();
		String sql = "INSERT INTO reviews (product_id, user_id, message, score) VALUES (?, ?, ?, ?)";

		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, productId);
			statement.setLong(2, userId);
			statement.setString(3, normalizedMessage);
			statement.setInt(4, score);
			return statement.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Review> findAdminPage(AdminReviewSearchRequest filters, int page, int size) {
		int normalizedPage = Math.max(page, 1);
		int normalizedSize = Math.max(size, 1);
		int offset = (normalizedPage - 1) * normalizedSize;

		StringBuilder sql = new StringBuilder("""
				SELECT r.*, p.name AS product_name,
				       CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, '')) AS user_display_name,
				       u.email AS user_email
				FROM reviews r
				JOIN products p ON p.id = r.product_id
				JOIN users u ON u.id = r.user_id
				WHERE 1=1
				""");
		List<Object> params = buildAdminFilterParams(sql, filters);
		sql.append(buildAdminOrderClause(filters));
		sql.append(" LIMIT ? OFFSET ?");

		List<Review> reviews = new ArrayList<>();
		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				statement.setObject(i + 1, params.get(i));
			}
			statement.setInt(params.size() + 1, normalizedSize);
			statement.setInt(params.size() + 2, offset);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					reviews.add(resultSetToReview(resultSet));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
		return reviews;
	}

	public long countAdmin(AdminReviewSearchRequest filters) {
		StringBuilder sql = new StringBuilder("""
				SELECT COUNT(*)
				FROM reviews r
				JOIN products p ON p.id = r.product_id
				JOIN users u ON u.id = r.user_id
				WHERE 1=1
				""");
		List<Object> params = buildAdminFilterParams(sql, filters);

		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				statement.setObject(i + 1, params.get(i));
			}
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

	public boolean deleteById(Long id) {
		if (id == null || id <= 0) {
			return false;
		}

		String sql = "DELETE FROM reviews WHERE id = ?";
		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, id);
			return statement.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public  long countByProductId(Long productId) {
		if (productId == null || productId <= 0) {
			return 0;
		}

		String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ?";
		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, productId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getLong(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public  Double findAverageScoreByProductId(Long productId) {
		if (productId == null || productId <= 0) {
			return 0.0;
		}

		String sql = "SELECT COALESCE(AVG(score), 0) FROM reviews WHERE product_id = ?";
		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, productId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getDouble(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}

	public  boolean existsByProductIdAndUserId(Long productId, Long userId) {
		if (productId == null || userId == null || productId <= 0 || userId <= 0) {
			return false;
		}

		String sql = "SELECT 1 FROM reviews WHERE product_id = ? AND user_id = ? LIMIT 1";
		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, productId);
			statement.setLong(2, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private List<Object> buildAdminFilterParams(StringBuilder sql, AdminReviewSearchRequest filters) {
		List<Object> params = new ArrayList<>();
		if (filters == null) {
			return params;
		}

		String keyword = filters.getKeyword() == null ? "" : filters.getKeyword().trim();
		if (!keyword.isEmpty()) {
			sql.append("""
					AND (r.message LIKE ?
					     OR p.name LIKE ?
					     OR u.email LIKE ?
					     OR u.username LIKE ?
					     OR CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, '')) LIKE ?)
					""");
			String like = "%" + keyword + "%";
			params.add(like);
			params.add(like);
			params.add(like);
			params.add(like);
			params.add(like);
		}
		if (filters.getScore() != null) {
			sql.append(" AND r.score = ?");
			params.add(filters.getScore());
		}
		if (filters.getProductId() != null) {
			sql.append(" AND r.product_id = ?");
			params.add(filters.getProductId());
		}
		if (filters.getFromDate() != null) {
			sql.append(" AND r.created_at >= ?");
			params.add(Timestamp.valueOf(filters.getFromDate().atStartOfDay()));
		}
		if (filters.getToDate() != null) {
			sql.append(" AND r.created_at <= ?");
			params.add(Timestamp.valueOf(filters.getToDate().atTime(LocalTime.MAX)));
		}
		return params;
	}

	private String buildAdminOrderClause(AdminReviewSearchRequest filters) {
		String sortBy = filters == null || filters.getSortBy() == null ? "" : filters.getSortBy().trim().toLowerCase();

		return switch (sortBy) {
			case "created_asc" -> " ORDER BY r.created_at ASC, r.id ASC";
			case "score_desc" -> " ORDER BY r.score DESC, r.created_at DESC, r.id DESC";
			case "score_asc" -> " ORDER BY r.score ASC, r.created_at DESC, r.id DESC";
			case "created_desc" -> " ORDER BY r.created_at DESC, r.id DESC";
			default -> " ORDER BY r.created_at DESC, r.id DESC";
		};
	}

	private Review resultSetToReview(ResultSet resultSet) throws SQLException {
		return Review.builder()
				.id(resultSet.getLong("id"))
				.productId(resultSet.getLong("product_id"))
				.userId(resultSet.getLong("user_id"))
				.productName(getNullableString(resultSet, "product_name"))
				.userDisplayName(getNullableString(resultSet, "user_display_name"))
				.userEmail(getNullableString(resultSet, "user_email"))
				.message(resultSet.getString("message"))
				.score(resultSet.getInt("score"))
				.createdAt(resultSet.getTimestamp("created_at"))
				.build();
	}

	private String getNullableString(ResultSet resultSet, String column) {
		try {
			return resultSet.getString(column);
		} catch (SQLException e) {
			return null;
		}
	}


}
