package com.daizuongkk.web.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.daizuongkk.web.dto.request.AdminProductSearchRequest;
import com.daizuongkk.web.dto.request.SearchProductRequest;
import com.daizuongkk.web.model.Product;
import com.daizuongkk.web.util.JDBCUtils;

public class ProductRepository {
	private static final String SQL = "SELECT p.* FROM products p ";
	private static volatile boolean softDeleteSchemaReady = false;

	public ProductRepository() {
		ensureSoftDeleteColumn();
	}

	public List<Product> findAll() {
		String sql = SQL + " WHERE p.deleted = 0 ORDER BY p.created_at DESC";
		return findManyBySql(sql, statement -> {
		});
	}

	public List<Product> findPage(int page, int size) {
		int normalizedPage = Math.max(page, 1);
		int normalizedSize = Math.max(size, 1);
		int offset = (normalizedPage - 1) * normalizedSize;

		String sql = SQL + " WHERE p.deleted = 0 ORDER BY RAND() LIMIT ? OFFSET ?";
		return findManyBySql(sql, statement -> {

			statement.setInt(1, normalizedSize);
			statement.setInt(2, offset);
		});
	}

	public List<Product> findByFilter(int page, int size, SearchProductRequest filters) {
		if (filters == null) {
			filters = SearchProductRequest.builder().build();
		}

		int normalizedPage = Math.max(page, 1);
		int normalizedSize = Math.max(size, 1);
		int offset = (normalizedPage - 1) * normalizedSize;

		StringBuilder sql = new StringBuilder(SQL + " WHERE p.deleted = 0");
		List<Object> params = buildFilterParams(sql, filters);

		// Add sorting
		sql.append(buildOrderClause(filters));
		sql.append(" LIMIT ? OFFSET ?");

		return findManyBySql(sql.toString(), statement -> {
			for (int i = 1; i <= params.size(); i++) {
				statement.setObject(i, params.get(i - 1));
			}

			statement.setInt(params.size() + 1, normalizedSize);
			statement.setInt(params.size() + 2, offset);
		});
	}

	public Product findById(Long id) {
		return findById(id, false);
	}

	public Product findByIdIncludingDeleted(Long id) {
		return findById(id, true);
	}

	private Product findById(Long id, boolean includeDeleted) {
		if (id == null) {
			return null;
		}

		String sql = SQL + " WHERE p.id = ?" + (includeDeleted ? "" : " AND p.deleted = 0") + " LIMIT 1";
		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSetToProduct(resultSet);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Product> findByCategory(String category) {
		if (category == null || category.trim().isEmpty()) {
			return Collections.emptyList();
		}

		String sql = SQL + " WHERE p.category = ? AND p.deleted = 0 ORDER BY p.created_at DESC";
		return findManyBySql(sql, statement -> statement.setString(1, category.trim()));
	}

	public List<Product> searchByName(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return Collections.emptyList();
		}

		String sql = SQL + " WHERE p.name LIKE ? AND p.deleted = 0 ORDER BY p.created_at DESC";
		return findManyBySql(sql, statement -> statement.setString(1, "%" + keyword.trim() + "%"));
	}

	public List<Product> findLatest(int limit) {
		int normalizedLimit = Math.max(limit, 1);
		String sql = "SELECT * FROM products WHERE deleted = 0 ORDER BY created_at DESC LIMIT ?";
		return findManyBySql(sql, statement -> statement.setInt(1, normalizedLimit));
	}

	public List<Product> findAdminPage(AdminProductSearchRequest filters, int page, int size) {
		int normalizedPage = Math.max(page, 1);
		int normalizedSize = Math.max(size, 1);
		int offset = (normalizedPage - 1) * normalizedSize;

		StringBuilder sql = new StringBuilder(SQL + " WHERE 1=1");
		List<Object> params = buildAdminFilterParams(sql, filters);
		sql.append(buildAdminOrderClause(filters));
		sql.append(" LIMIT ? OFFSET ?");

		return findManyBySql(sql.toString(), statement -> {
			for (int i = 0; i < params.size(); i++) {
				statement.setObject(i + 1, params.get(i));
			}
			statement.setInt(params.size() + 1, normalizedSize);
			statement.setInt(params.size() + 2, offset);
		});
	}

	public Long countAdmin(AdminProductSearchRequest filters) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p WHERE 1=1");
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

	private List<Object> buildAdminFilterParams(StringBuilder sql, AdminProductSearchRequest filters) {
		List<Object> params = new ArrayList<>();
		if (filters == null) {
			return params;
		}

		String keyword = filters.getKeyword() == null ? "" : filters.getKeyword().trim();
		if (!keyword.isEmpty()) {
			sql.append(" AND (p.name LIKE ? OR p.description LIKE ? OR p.summary LIKE ? OR p.brand LIKE ?)");
			String like = "%" + keyword + "%";
			params.add(like);
			params.add(like);
			params.add(like);
			params.add(like);
		}
		if (filters.getCategory() != null && !filters.getCategory().isBlank()) {
			sql.append(" AND p.category = ?");
			params.add(filters.getCategory().trim());
		}
		if (filters.getBrand() != null && !filters.getBrand().isBlank()) {
			sql.append(" AND p.brand = ?");
			params.add(filters.getBrand().trim());
		}
		if (filters.getMinPrice() != null) {
			sql.append(" AND p.price >= ?");
			params.add(filters.getMinPrice());
		}
		if (filters.getMaxPrice() != null) {
			sql.append(" AND p.price <= ?");
			params.add(filters.getMaxPrice());
		}
		if (filters.getMinQuantity() != null) {
			sql.append(" AND p.quantity >= ?");
			params.add(filters.getMinQuantity());
		}
		if (filters.getMaxQuantity() != null) {
			sql.append(" AND p.quantity <= ?");
			params.add(filters.getMaxQuantity());
		}
		if ("active".equalsIgnoreCase(filters.getDeleted())) {
			sql.append(" AND p.deleted = 0");
		} else if ("deleted".equalsIgnoreCase(filters.getDeleted())) {
			sql.append(" AND p.deleted = 1");
		}
		return params;
	}

	private String buildAdminOrderClause(AdminProductSearchRequest filters) {
		String sortBy = filters == null || filters.getSortBy() == null ? "" : filters.getSortBy().trim().toLowerCase();

		return switch (sortBy) {
			case "quantity_asc" -> " ORDER BY p.quantity ASC, p.created_at DESC, p.id DESC";
			case "quantity_desc" -> " ORDER BY p.quantity DESC, p.created_at DESC, p.id DESC";
			case "created_asc" -> " ORDER BY p.created_at ASC, p.id ASC";
			case "deleted_asc" -> " ORDER BY p.deleted ASC, p.created_at DESC, p.id DESC";
			case "deleted_desc" -> " ORDER BY p.deleted DESC, p.created_at DESC, p.id DESC";
			case "created_desc" -> " ORDER BY p.created_at DESC, p.id DESC";
			default -> " ORDER BY p.created_at DESC, p.id DESC";
		};
	}

	public List<Product> findLowStock(int limit) {
		int normalizedLimit = Math.max(limit, 1);
		String sql = SQL + " WHERE p.deleted = 0 ORDER BY p.quantity ASC, p.updated_at DESC LIMIT ?";
		return findManyBySql(sql, statement -> statement.setInt(1, normalizedLimit));
	}

	public Product save(Product product) {
		if (product == null || product.getName() == null || product.getName().trim().isEmpty()) {
			return null;
		}

		String sql = "INSERT INTO products (name, description, detail, summary, price, brand, category, promotion, quantity) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, product.getName().trim());
			statement.setString(2, product.getDescription());
			statement.setString(3, product.getDetail());
			statement.setString(4, product.getSummary());
			if (product.getPrice() == null) {
				statement.setNull(5, java.sql.Types.DECIMAL);
			} else {
				statement.setDouble(5, product.getPrice());
			}
			statement.setString(6, product.getBrand());
			statement.setString(7, product.getCategory());
			statement.setLong(8, product.getPromotion() == null ? 0L : product.getPromotion());
			statement.setLong(9, product.getQuantity() == null ? 0L : product.getQuantity());

			if (statement.executeUpdate() == 0) {
				return null;
			}
			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					product.setId(generatedKeys.getLong(1));
				}
			}
			return product;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean update(Product product) {
		if (product == null || product.getId() == null || product.getId() <= 0
				|| product.getName() == null || product.getName().trim().isEmpty()) {
			return false;
		}

		String sql = "UPDATE products SET name = ?, description = ?, detail = ?, summary = ?, price = ?, brand = ?, "
				+ "category = ?, promotion = ?, quantity = ?, deleted = 0 WHERE id = ?";

		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, product.getName().trim());
			statement.setString(2, product.getDescription());
			statement.setString(3, product.getDetail());
			statement.setString(4, product.getSummary());
			if (product.getPrice() == null) {
				statement.setNull(5, java.sql.Types.DECIMAL);
			} else {
				statement.setDouble(5, product.getPrice());
			}
			statement.setString(6, product.getBrand());
			statement.setString(7, product.getCategory());
			statement.setLong(8, product.getPromotion() == null ? 0L : product.getPromotion());
			statement.setLong(9, product.getQuantity() == null ? 0L : product.getQuantity());
			statement.setLong(10, product.getId());
			return statement.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteById(Long id) {
		if (id == null || id <= 0) {
			return false;
		}

		String sql = "UPDATE products SET deleted = 1 WHERE id = ? AND deleted = 0";
		try (Connection connection = JDBCUtils.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, id);
			return statement.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Long countAll() {
		String sql = "SELECT COUNT(*) FROM products WHERE deleted = 0";
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

	public Long countByCategory(String category) {
		if (category == null || category.trim().isEmpty()) {
			return 0L;
		}
		String sql = "SELECT COUNT(*) FROM products WHERE category = ? AND deleted = 0";
		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, category.trim());

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

	public Long countByFilter(SearchProductRequest filters) {
		if (filters == null) {
			filters = SearchProductRequest.builder().build();
		}

		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p WHERE p.deleted = 0 ");
		List<Object> params = buildFilterParams(sql, filters);

		// 1. Chỉ khởi tạo PreparedStatement trước
		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql.toString())) {

			// 2. PHẢI Set params TRƯỚC KHI execute
			for (int i = 0; i < params.size(); i++) {
				statement.setObject(i + 1, params.get(i));
			}

			// 3. Bây giờ mới thực thi và lấy ResultSet
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

	private String buildOrderClause(SearchProductRequest filters) {
		if (filters == null || filters.getSortBy() == null || filters.getSortBy().isBlank()) {
			return " ";
		}

		return switch (filters.getSortBy().trim().toLowerCase()) {
			case "price_asc" -> " ORDER BY p.price ASC";
			case "price_desc" -> " ORDER BY p.price DESC";
			case "newest" -> " ORDER BY p.created_at DESC";
			case "oldest" -> " ORDER BY p.created_at ASC";
			default -> "";
		};
	}

	private List<Object> buildFilterParams(StringBuilder sql, SearchProductRequest filters) {
		List<Object> params = new ArrayList<>();

		if (filters == null) {
			return params;
		}

		List<String> categories = filters.getCategories();

		String name = filters.getName();

		if (name != null && !name.isBlank()) {
			sql.append(" AND MATCH(p.name, p.description, p.detail, p.summary) AGAINST(? IN NATURAL LANGUAGE MODE)");
			params.add(name.trim());
		}

		if (categories != null && !categories.isEmpty()) {
			sql.append(" AND p.category IN (").append(String.join(",", categories.stream().map(c -> "?").toList()))
					.append(")");
			params.addAll(categories);
		}
		List<String> brands = filters.getBrands();

		if (brands != null && !brands.isEmpty()) {
			sql.append(" AND p.brand IN (").append(String.join(",", brands.stream().map(b -> "?").toList())).append(")");
			params.addAll(brands);
		}

		Double minPrice = filters.getMinPrice();
		if (minPrice != null) {
			sql.append(" AND p.price >= ?");
			params.add(minPrice);
		}

		Double maxPrice = filters.getMaxPrice();
		if (maxPrice != null) {
			sql.append(" AND p.price <= ?");
			params.add(maxPrice);
		}

		return params;
	}

	private List<Product> findManyBySql(String sql, StatementBinder binder) {
		List<Product> products = new ArrayList<>();
		try (Connection connection = JDBCUtils.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			binder.bind(statement);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					products.add(resultSetToProduct(resultSet));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return products;
	}

	private Product resultSetToProduct(ResultSet resultSet) throws SQLException {
		Double price = null;
		double rawPrice = resultSet.getDouble("price");
		if (!resultSet.wasNull()) {
			price = rawPrice;
		}

		return Product.builder()
				.id(resultSet.getLong("id"))
				.name(resultSet.getString("name"))
				.description(resultSet.getString("description"))
				.detail(resultSet.getString("detail"))
				.summary(resultSet.getString("summary"))
				.category(resultSet.getString("category"))
				.price(price)
				.promotion(resultSet.getLong("promotion"))
				.quantity(resultSet.getLong("quantity"))
				.brand(resultSet.getString("brand"))
				.createdAt(resultSet.getTimestamp("created_at"))
				.updatedAt(resultSet.getTimestamp("updated_at"))
				.deleted(resultSet.getBoolean("deleted"))
				.build();
	}

	private void ensureSoftDeleteColumn() {
		if (softDeleteSchemaReady) {
			return;
		}

		synchronized (ProductRepository.class) {
			if (softDeleteSchemaReady) {
				return;
			}

			try (Connection connection = JDBCUtils.getConnection();
				 Statement statement = connection.createStatement()) {
				if (!columnExists(connection, "products", "deleted")) {
					statement.executeUpdate("ALTER TABLE products ADD COLUMN deleted TINYINT(1) DEFAULT 0 AFTER view");
				}
				softDeleteSchemaReady = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean columnExists(Connection connection, String table, String column) throws SQLException {
		String sql = """
				SELECT 1
				FROM information_schema.COLUMNS
				WHERE TABLE_SCHEMA = DATABASE()
				  AND TABLE_NAME = ?
				  AND COLUMN_NAME = ?
				LIMIT 1
				""";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, table);
			statement.setString(2, column);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	@FunctionalInterface
	private interface StatementBinder {
		void bind(PreparedStatement statement) throws SQLException;
	}

}
