package com.daizuongkk.web.repository;

import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.model.Role;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserRepository   {


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
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getLastName());
            statement.setString(6, user.getPhone());
            statement.setString(7, user.getRole().name());
            statement.setString(8, user.getStatus() == null ? "ACTIVE" : user.getStatus());
            statement.setBoolean(9, user.getVerified() != null && user.getVerified());
            return statement.executeUpdate() > 0;
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

}
