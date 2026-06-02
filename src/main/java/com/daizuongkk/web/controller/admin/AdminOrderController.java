package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.request.AdminOrderSearchRequest;
import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.service.DashboardReportExcelExporter;
import com.daizuongkk.web.service.OrderService;
import com.daizuongkk.web.util.PaginationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "AdminOrderController", value = { "/admin/orders", "/admin/orders/export" })
public class AdminOrderController extends BaseAdminServlet {
	private final OrderService orderService = new OrderService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!requireAdmin(request, response)) {
			return;
		}

		int page = PaginationUtils.parsePositiveInt(request.getParameter("p"), 1);
		int size = PaginationUtils.parsePositiveInt(request.getParameter("size"), 10);
		AdminOrderSearchRequest filters = buildFilters(request);
		if (request.getServletPath().endsWith("/export")) {
			exportOrders(request, response, filters);
			return;
		}

		long totalOrders = orderService.countAdminOrders(filters);
		int totalPages = Math.max(1, (int) Math.ceil(totalOrders / (double) size));
		if (page > totalPages) {
			page = totalPages;
		}

		List<Order> orders = orderService.findAdminOrders(filters, page, size);
		request.setAttribute("orders", orders);
		request.setAttribute("currentPage", page);
		request.setAttribute("pageSize", size);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalOrders", totalOrders);
		setFilterAttributes(request, filters);
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
		redirectBackToOrders(request, response);
	}

	private AdminOrderSearchRequest buildFilters(HttpServletRequest request) {
		return AdminOrderSearchRequest.builder()
				.keyword(trim(request.getParameter("keyword")))
				.status(normalizeStatus(request.getParameter("status")))
				.minTotal(parseDouble(request.getParameter("minTotal")))
				.maxTotal(parseDouble(request.getParameter("maxTotal")))
				.fromDate(parseDate(request.getParameter("fromDate")))
				.toDate(parseDate(request.getParameter("toDate")))
				.sortBy(normalizeSort(request.getParameter("sortBy")))
				.build();
	}

	private void setFilterAttributes(HttpServletRequest request, AdminOrderSearchRequest filters) {
		request.setAttribute("keyword", filters.getKeyword() == null ? "" : filters.getKeyword());
		request.setAttribute("selectedStatus", filters.getStatus() == null ? "" : filters.getStatus());
		request.setAttribute("minTotal", filters.getMinTotal());
		request.setAttribute("maxTotal", filters.getMaxTotal());
		request.setAttribute("fromDate", filters.getFromDate() == null ? "" : filters.getFromDate().toString());
		request.setAttribute("toDate", filters.getToDate() == null ? "" : filters.getToDate().toString());
		request.setAttribute("selectedSortBy", filters.getSortBy() == null ? "created_desc" : filters.getSortBy());
	}

	private void exportOrders(HttpServletRequest request, HttpServletResponse response, AdminOrderSearchRequest filters)
			throws IOException {
		List<Order> orders = orderService.findAdminOrdersForExport(filters);
		String filename = "bao-cao-don-hang-" + LocalDate.now() + ".xlsx";
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''"
				+ URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));
		new DashboardReportExcelExporter().exportOrders(orders, filters, response.getOutputStream());
	}

	private void redirectBackToOrders(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String returnUrl = request.getParameter("returnUrl");
		String adminOrdersPath = request.getContextPath() + "/admin/orders";
		if (returnUrl != null && returnUrl.startsWith(adminOrdersPath)) {
			response.sendRedirect(returnUrl);
			return;
		}
		response.sendRedirect(adminOrdersPath);
	}

	private String normalizeStatus(String value) {
		String status = trim(value);
		if (status == null || status.isBlank()) {
			return "";
		}
		return switch (status.toUpperCase()) {
			case "PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED" -> status.toUpperCase();
			default -> "";
		};
	}

	private String normalizeSort(String value) {
		String sortBy = trim(value);
		if (sortBy == null || sortBy.isBlank()) {
			return "created_desc";
		}
		return switch (sortBy.toLowerCase()) {
			case "created_asc", "created_desc", "total_asc", "total_desc", "status_asc" -> sortBy.toLowerCase();
			default -> "created_desc";
		};
	}

	private LocalDate parseDate(String value) {
		try {
			return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
		} catch (Exception e) {
			return null;
		}
	}
}
