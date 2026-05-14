package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminOrderController", value = "/admin/orders")
public class AdminOrderController extends BaseAdminServlet {
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        List<Order> orders = orderService.findAllOrders();
        request.setAttribute("orders", orders);
        forward(request, response, "admin-orders.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        Long orderId = parseLong(request.getParameter("id"));
        String action = request.getParameter("action");
        orderService.adminUpdateStatus(orderId, action);
        response.sendRedirect(request.getContextPath() + "/admin/orders");
    }
}
