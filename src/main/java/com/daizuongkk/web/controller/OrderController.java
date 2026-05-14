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
import java.util.Locale;

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
		String activeTab = normalizeTab(request.getParameter("tab"));
		Order selectedOrder = null;

		if (orderIdParam != null && !orderIdParam.isBlank()) {
			Long orderId = parseLong(orderIdParam);
			Order order = orderService.findOrder(orderId, account.getId());
			if (order == null) {
				response.sendRedirect(request.getContextPath() + "/orders");
				return;
			}
			selectedOrder = order;
		} else if (placedOrderId != null) {
			Order order = orderService.findOrder(placedOrderId, account.getId());
			if (order != null) {
				selectedOrder = order;
			}
		}

		if (selectedOrder != null && request.getParameter("tab") == null) {
			activeTab = tabForStatus(selectedOrder.getStatus());
		}

		final String selectedTab = activeTab;
		List<Order> allOrders = orderService.findOrdersByUserId(account.getId());
		List<Order> orders = allOrders.stream()
				.filter(order -> selectedTab.equals(tabForStatus(order.getStatus())))
				.toList();
		request.getSession().setAttribute("orderCount", orderService.countOrdersByUserId(account.getId()));
		request.setAttribute("orders", orders);
		request.setAttribute("selectedOrder", selectedOrder);
		request.setAttribute("placedOrderId", placedOrderId);
		request.setAttribute("activeOrderTab", activeTab);
		request.setAttribute("activeOrderCount", countByTab(allOrders, "active"));
		request.setAttribute("completedOrderCount", countByTab(allOrders, "completed"));
		request.setAttribute("cancelledOrderCount", countByTab(allOrders, "cancelled"));
		request.getRequestDispatcher("/views/pages/orders.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		UserResponse account = getAccount(request);
		Long orderId = parseLong(request.getParameter("id"));
		String action = request.getParameter("action");
		String tab = normalizeTab(request.getParameter("tab"));

		if ("cancel".equals(action) && orderService.cancelOrder(orderId, account.getId())) {
			response.sendRedirect(request.getContextPath() + "/orders?tab=cancelled&id=" + orderId + "&cancelled=1");
			return;
		}

		response
				.sendRedirect(request.getContextPath() + "/orders?tab=" + tab + "&id=" + (orderId == null ? "" : orderId) + "&error=cancel");
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

	private String normalizeTab(String tab) {
		if (tab == null || tab.isBlank()) {
			return "active";
		}

		String normalized = tab.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "cancelled", "completed", "active" -> normalized;
			default -> "active";
		};
	}

	private String tabForStatus(String status) {
		if ("CANCELLED".equalsIgnoreCase(status)) {
			return "cancelled";
		}
		if ("COMPLETED".equalsIgnoreCase(status)) {
			return "completed";
		}
		return "active";
	}

	private long countByTab(List<Order> orders, String tab) {
		return orders.stream()
				.filter(order -> tab.equals(tabForStatus(order.getStatus())))
				.count();
	}
}
