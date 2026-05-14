package com.daizuongkk.web.repository;

import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.model.Role;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserRepository   {
    private static volatile boolean verificationSchemaReady = false;

    public UserRepository() {
        ensureVerificationSchema();
    }

    public  User findByUsernameOrEmail(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, username.trim());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSetToUser(resultSet);
            } else {
                return null;
            }
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    private User resultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        user.setAvtUrl(resultSet.getString("avt_url"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setPhone(resultSet.getString("phone"));
        user.setEmail(resultSet.getString("email"));
        user.setStatus(resultSet.getString("status"));
        user.setVerified(resultSet.getBoolean("verified"));
        user.setPhoneVerified(resultSet.getBoolean("phone_verified"));
        user.setLastLogin(resultSet.getTimestamp("last_login"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        user.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return user;
    }

    public  boolean existsByUsername(String username) {
        return exists("SELECT 1 FROM users WHERE username = ? LIMIT 1", username);
    }

    public  boolean existsByEmail(String email) {
        return exists("SELECT 1 FROM users WHERE email = ? LIMIT 1", email);
    }

    public  boolean create(User user) {
        String sql = "INSERT INTO users (username, email, password, first_name, last_name, phone, role, status, verified) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String cartSql = "INSERT INTO carts (user_id) VALUES (?)";

        try (Connection connection = JDBCUtils.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getEmail());
                statement.setString(3, user.getPassword());
                statement.setString(4, user.getFirstName());
                statement.setString(5, user.getLastName());
                statement.setString(6, user.getPhone());
                statement.setString(7, user.getRole().name());
                statement.setString(8, user.getStatus() == null ? "ACTIVE" : user.getStatus());
                statement.setBoolean(9, user.getVerified() != null && user.getVerified());
                if (statement.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        connection.rollback();
                        return false;
                    }

                    Long userId = generatedKeys.getLong(1);
                    user.setId(userId);

                    try (PreparedStatement cartStatement = connection.prepareStatement(cartSql)) {
                        cartStatement.setLong(1, userId);
                        if (cartStatement.executeUpdate() == 0) {
                            connection.rollback();
                            return false;
                        }
                    }
                }
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean exists(String sql, String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        if (id == null) {
            return null;
        }

        User user = null;
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                   user =  resultSetToUser(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }

    public List<User> findPage(AdminUserSearchRequest filters, int page, int size) {
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.max(size, 1);
        int offset = (normalizedPage - 1) * normalizedSize;

        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = buildAdminFilterParams(sql, filters);
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

        List<User> users = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            statement.setInt(params.size() + 1, normalizedSize);
            statement.setInt(params.size() + 2, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(resultSetToUser(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return users;
    }

    public Long count(AdminUserSearchRequest filters) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
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

    private List<Object> buildAdminFilterParams(StringBuilder sql, AdminUserSearchRequest filters) {
        List<Object> params = new ArrayList<>();
        if (filters == null) {
            return params;
        }

        String keyword = filters.getKeyword() == null ? "" : filters.getKeyword().trim();
        if (!keyword.isEmpty()) {
            sql.append(" AND (username LIKE ? OR email LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR phone LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (filters.getRole() != null) {
            sql.append(" AND role = ?");
            params.add(filters.getRole().name());
        }
        if (filters.getStatus() != null && !filters.getStatus().isBlank()) {
            sql.append(" AND status = ?");
            params.add(filters.getStatus().trim().toUpperCase());
        }
        if (filters.getVerified() != null) {
            sql.append(" AND verified = ?");
            params.add(filters.getVerified());
        }
        return params;
    }

    public boolean updateStatus(Long id, String status) {
        if (id == null || id <= 0 || status == null || status.trim().isEmpty()) {
            return false;
        }

        String normalizedStatus = status.trim().toUpperCase();
        if (!normalizedStatus.equals("ACTIVE") && !normalizedStatus.equals("INACTIVE") && !normalizedStatus.equals("BANNED")) {
            return false;
        }

        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedStatus);
            statement.setLong(2, id);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(User user, boolean updatePassword) {
        if (user == null || user.getId() == null || user.getId() <= 0) {
            return false;
        }

        String sql = updatePassword
                ? "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, phone = ?, role = ?, status = ?, verified = ?, password = ? WHERE id = ?"
                : "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, phone = ?, role = ?, status = ?, verified = ? WHERE id = ?";

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getRole().name());
            statement.setString(7, user.getStatus());
            statement.setBoolean(8, user.getVerified() != null && user.getVerified());
            if (updatePassword) {
                statement.setString(9, user.getPassword());
                statement.setLong(10, user.getId());
            } else {
                statement.setLong(9, user.getId());
            }
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProfile(User user, String passwordHash) {
        if (user == null || user.getId() == null || user.getId() <= 0) {
            return false;
        }

        boolean updatePassword = passwordHash != null && !passwordHash.isBlank();
        String sql = updatePassword
                ? "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, phone = ?, avt_url = ?, verified = ?, phone_verified = ?, password = ? WHERE id = ?"
                : "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, phone = ?, avt_url = ?, verified = ?, phone_verified = ? WHERE id = ?";

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            User currentUser = findById(user.getId());
            boolean emailVerified = currentUser != null
                    && Boolean.TRUE.equals(currentUser.getVerified())
                    && equalsNullable(currentUser.getEmail(), user.getEmail());
            boolean phoneVerified = currentUser != null
                    && Boolean.TRUE.equals(currentUser.getPhoneVerified())
                    && equalsNullable(currentUser.getPhone(), user.getPhone());

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getAvtUrl());
            statement.setBoolean(7, emailVerified);
            statement.setBoolean(8, phoneVerified);
            if (updatePassword) {
                statement.setString(9, passwordHash);
                statement.setLong(10, user.getId());
            } else {
                statement.setLong(9, user.getId());
            }
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEmailVerified(Long userId, boolean verified) {
        return updateBooleanColumn(userId, "verified", verified);
    }

    public boolean updateEmailAndVerified(Long userId, String email) {
        if (userId == null || userId <= 0 || email == null || email.trim().isEmpty()) {
            return false;
        }

        String sql = "UPDATE users SET email = ?, verified = 1 WHERE id = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim().toLowerCase());
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePhoneVerified(Long userId, boolean verified) {
        return updateBooleanColumn(userId, "phone_verified", verified);
    }

    public boolean updatePhoneAndVerified(Long userId, String phone) {
        if (userId == null || userId <= 0 || phone == null || phone.trim().isEmpty()) {
            return false;
        }

        String sql = "UPDATE users SET phone = ?, phone_verified = 1 WHERE id = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone.trim());
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(Long userId, String passwordHash) {
        if (userId == null || userId <= 0 || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }

        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateBooleanColumn(Long userId, String column, boolean value) {
        if (userId == null || userId <= 0) {
            return false;
        }

        String sql = "UPDATE users SET " + column + " = ? WHERE id = ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, value);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean equalsNullable(String first, String second) {
        String normalizedFirst = first == null ? "" : first.trim();
        String normalizedSecond = second == null ? "" : second.trim();
        return normalizedFirst.equalsIgnoreCase(normalizedSecond);
    }

    private void ensureVerificationSchema() {
        if (verificationSchemaReady) {
            return;
        }

        synchronized (UserRepository.class) {
            if (verificationSchemaReady) {
                return;
            }

            try (Connection connection = JDBCUtils.getConnection();
                 Statement statement = connection.createStatement()) {
                if (!columnExists(connection, "users", "phone_verified")) {
                    statement.executeUpdate("ALTER TABLE users ADD COLUMN phone_verified TINYINT(1) DEFAULT 0 AFTER verified");
                }
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS verification_otps (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            channel VARCHAR(20) NOT NULL,
                            target_value VARCHAR(150) NOT NULL,
                            otp_hash VARCHAR(100) NOT NULL,
                            expires_at DATETIME NOT NULL,
                            consumed TINYINT(1) DEFAULT 0,
                            attempts INT DEFAULT 0,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_verification_otps_lookup (user_id, channel, consumed, expires_at),
                            CONSTRAINT fk_verification_otps_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                        )
                        """);
                verificationSchemaReady = true;
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

}
