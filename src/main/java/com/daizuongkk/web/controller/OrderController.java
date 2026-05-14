package com.daizuongkk.web.controller;

import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "Orders", value = "/orders")
public class OrderController extends HttpServlet {
    private OrderService orderService;

    @Override
    public void init() {
        this.orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserResponse account = getAccount(request);
        String orderIdParam = request.getParameter("id");
        Long placedOrderId = parseLong(request.getParameter("placed"));

        if (orderIdParam != null && !orderIdParam.isBlank()) {
            Long orderId = parseLong(orderIdParam);
            Order order = orderService.findOrder(orderId, account.getId());
            if (order == null) {
                response.sendRedirect(request.getContextPath() + "/orders");
                return;
            }
            request.setAttribute("selectedOrder", order);
        } else if (placedOrderId != null) {
            Order order = orderService.findOrder(placedOrderId, account.getId());
            if (order != null) {
                request.setAttribute("selectedOrder", order);
            }
        }

        List<Order> orders = orderService.findOrdersByUserId(account.getId());
        request.getSession().setAttribute("orderCount", orderService.countOrdersByUserId(account.getId()));
        request.setAttribute("orders", orders);
        request.setAttribute("placedOrderId", placedOrderId);
        request.getRequestDispatcher("/views/pages/orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        UserResponse account = getAccount(request);
        Long orderId = parseLong(request.getParameter("id"));
        String action = request.getParameter("action");

        if ("cancel".equals(action) && orderService.cancelOrder(orderId, account.getId())) {
            response.sendRedirect(request.getContextPath() + "/orders?id=" + orderId + "&cancelled=1");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/orders?id=" + (orderId == null ? "" : orderId) + "&error=cancel");
    }

    private UserResponse getAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (UserResponse) session.getAttribute("account");
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
