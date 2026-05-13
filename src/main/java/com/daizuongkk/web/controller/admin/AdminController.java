package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.service.ProductService;
import com.daizuongkk.web.service.UserService;
import com.daizuongkk.web.util.JDBCUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminController", value = "/admin")
public class AdminController extends BaseAdminServlet {

    private final ProductService productService = new ProductService();
    private final UserService userService = new UserService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        String page = request.getParameter("page");
        if (page == null || page.isBlank() || "admin-dashboard".equals(page)) {
            loadDashboard(request);
            forward(request, response, "admin-dashboard.jsp");
            return;
        }

        switch (page) {
            case "inventory":
                response.sendRedirect(request.getContextPath() + "/admin/products");
                return;
            case "create-product":
            case "add-product":
                response.sendRedirect(request.getContextPath() + "/admin/products/form");
                return;
            case "users":
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            case "create-user":
                response.sendRedirect(request.getContextPath() + "/admin/users/form");
                return;
            default:
                response.sendRedirect(request.getContextPath() + "/admin");
        }
    }

    private void loadDashboard(HttpServletRequest request) {
        request.setAttribute("totalProducts", productService.countProducts());
        request.setAttribute("totalUsers", userService.countUsers(AdminUserSearchRequest.builder().build()));
        request.setAttribute("totalOrders", queryLong("SELECT COUNT(*) FROM orders"));
        request.setAttribute("totalRevenue", queryDouble("SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE status IN ('PAID','SHIPPED','COMPLETED')"));
        request.setAttribute("pendingOrders", queryLong("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'"));
        request.setAttribute("completedOrders", queryLong("SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'"));
        request.setAttribute("lowStockProducts", productService.getLowStockProducts(5));
        request.setAttribute("latestProducts", productService.getLatestProducts(5));
        request.setAttribute("recentOrders", queryRecentOrders(5));
    }

    private Long queryLong(String sql) {
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

    private Double queryDouble(String sql) {
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0D;
    }

    private List<Map<String, Object>> queryRecentOrders(int limit) {
        List<Map<String, Object>> orders = new ArrayList<>();
        String sql = "SELECT id, recipient_name, phone, total_price, status, created_at FROM orders ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(limit, 1));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> order = new HashMap<>();
                    order.put("id", resultSet.getLong("id"));
                    order.put("recipientName", resultSet.getString("recipient_name"));
                    order.put("phone", resultSet.getString("phone"));
                    order.put("totalPrice", resultSet.getDouble("total_price"));
                    order.put("status", resultSet.getString("status"));
                    order.put("createdAt", resultSet.getTimestamp("created_at"));
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }
}
