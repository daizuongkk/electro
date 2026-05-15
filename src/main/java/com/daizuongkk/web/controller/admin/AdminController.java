package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.response.DashboardResponse;
import com.daizuongkk.web.service.DashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminController", value = "/admin")
public class AdminController extends BaseAdminServlet {

    private final DashboardService dashboardService = new DashboardService();

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
            case "orders":
                response.sendRedirect(request.getContextPath() + "/admin/orders");
                return;
            case "create-user":
                response.sendRedirect(request.getContextPath() + "/admin/users/form");
                return;
            default:
                response.sendRedirect(request.getContextPath() + "/admin");
        }
    }

    private void loadDashboard(HttpServletRequest request) {
        DashboardResponse dashboard = dashboardService.getDashboard();
        request.setAttribute("totalProducts", dashboard.getTotalProducts());
        request.setAttribute("totalUsers", dashboard.getTotalUsers());
        request.setAttribute("totalOrders", dashboard.getTotalOrders());
        request.setAttribute("totalRevenue", dashboard.getTotalRevenue());
        request.setAttribute("pendingOrders", dashboard.getPendingOrders());
        request.setAttribute("completedOrders", dashboard.getCompletedOrders());
        request.setAttribute("lowStockProducts", dashboard.getLowStockProducts());
        request.setAttribute("latestProducts", dashboard.getLatestProducts());
        request.setAttribute("recentOrders", dashboard.getRecentOrders());
        request.setAttribute("monthlySalesData", dashboard.getMonthlySalesData());
        request.setAttribute("customerOverview", dashboard.getCustomerOverview());
    }
}
