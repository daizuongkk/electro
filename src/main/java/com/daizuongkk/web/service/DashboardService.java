package com.daizuongkk.web.service;

import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.daizuongkk.web.dto.request.AdminOrderSearchRequest;
import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.dto.response.CustomerOverviewResponse;
import com.daizuongkk.web.dto.response.DashboardReportResponse;
import com.daizuongkk.web.dto.response.DashboardResponse;
import com.daizuongkk.web.dto.response.MonthlySalesResponse;
import com.daizuongkk.web.dto.response.OrderStatusReportResponse;
import com.daizuongkk.web.dto.response.SalesTrendResponse;
import com.daizuongkk.web.model.MonthlySalesStat;
import com.daizuongkk.web.repository.DashboardRepository;
import com.daizuongkk.web.repository.OrderRepository;

public class DashboardService {
	private static final String[] REPORT_STATUSES = { "PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED" };

	private final DashboardRepository dashboardRepository;
	private final ProductService productService;
	private final UserService userService;
	private final OrderRepository orderRepository;

	public DashboardService() {
		this(new DashboardRepository(), new ProductService(), new UserService(), new OrderRepository());
	}

	public DashboardService(DashboardRepository dashboardRepository, ProductService productService,
			UserService userService) {
		this(dashboardRepository, productService, userService, new OrderRepository());
	}

	public DashboardService(DashboardRepository dashboardRepository, ProductService productService,
			UserService userService, OrderRepository orderRepository) {
		this.dashboardRepository = dashboardRepository != null ? dashboardRepository : new DashboardRepository();
		this.productService = productService != null ? productService : new ProductService();
		this.userService = userService != null ? userService : new UserService();
		this.orderRepository = orderRepository != null ? orderRepository : new OrderRepository();
	}

	public DashboardResponse getDashboard() {
		return DashboardResponse.builder()
				.totalProducts(productService.countProducts())
				.totalUsers(userService.countUsers(AdminUserSearchRequest.builder().deleted("active").build()))
				.totalOrders(dashboardRepository.countOrders())
				.totalRevenue(dashboardRepository.sumValidOrderRevenue())
				.pendingOrders(dashboardRepository.countOrdersByStatus("PENDING"))
				.completedOrders(dashboardRepository.countOrdersByStatus("COMPLETED"))
				.lowStockProducts(productService.getLowStockProducts(5))
				.latestProducts(productService.getLatestProducts(5))
				.recentOrders(dashboardRepository.findRecentOrders(5))
				.monthlySalesData(getMonthlySalesData(12))
				.customerOverview(getCustomerOverview())
				.build();
	}

	public DashboardReportResponse getReport(LocalDate fromDate, LocalDate toDate) {
		LocalDate safeFromDate = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
		LocalDate safeToDate = toDate == null ? LocalDate.now() : toDate;
		if (safeFromDate.isAfter(safeToDate)) {
			LocalDate temp = safeFromDate;
			safeFromDate = safeToDate;
			safeToDate = temp;
		}

		long validOrders = dashboardRepository.countValidOrdersBetween(safeFromDate, safeToDate);
		double validRevenue = dashboardRepository.sumValidOrderRevenueBetween(safeFromDate, safeToDate);
		List<SalesTrendResponse> salesTrend = fillSalesTrendGaps(safeFromDate, safeToDate,
				dashboardRepository.findSalesTrend(safeFromDate, safeToDate));
		List<OrderStatusReportResponse> statusBreakdown = fillStatusBreakdownGaps(
				dashboardRepository.findStatusBreakdown(safeFromDate, safeToDate));

		return DashboardReportResponse.builder()
				.fromDate(safeFromDate)
				.toDate(safeToDate)
				.generatedAt(LocalDateTime.now())
				.totalOrders(dashboardRepository.countOrdersBetween(safeFromDate, safeToDate))
				.validOrders(validOrders)
				.validRevenue(validRevenue)
				.pendingOrders(dashboardRepository.countOrdersByStatusBetween("PENDING", safeFromDate, safeToDate))
				.paidOrders(dashboardRepository.countOrdersByStatusBetween("PAID", safeFromDate, safeToDate))
				.shippedOrders(dashboardRepository.countOrdersByStatusBetween("SHIPPED", safeFromDate, safeToDate))
				.completedOrders(dashboardRepository.countOrdersByStatusBetween("COMPLETED", safeFromDate, safeToDate))
				.cancelledOrders(dashboardRepository.countOrdersByStatusBetween("CANCELLED", safeFromDate, safeToDate))
				.uniqueCustomers(dashboardRepository.countUniqueBuyingCustomersBetween(safeFromDate, safeToDate))
				.itemsSold(dashboardRepository.sumItemsSoldBetween(safeFromDate, safeToDate))
				.averageOrderValue(validOrders > 0 ? validRevenue / validOrders : 0D)
				.salesTrend(salesTrend)
				.statusBreakdown(statusBreakdown)
				.topProducts(dashboardRepository.findTopSellingProducts(safeFromDate, safeToDate, 10))
				.recentOrders(dashboardRepository.findRecentOrdersBetween(safeFromDate, safeToDate, 10))
				.orders(orderRepository.findAdminForExport(AdminOrderSearchRequest.builder()
						.fromDate(safeFromDate)
						.toDate(safeToDate)
						.sortBy("created_desc")
						.build(), 10000))
				.build();
	}

	private List<SalesTrendResponse> fillSalesTrendGaps(LocalDate fromDate, LocalDate toDate,
			List<SalesTrendResponse> actualData) {
		boolean groupByMonth = fromDate.plusDays(62).isBefore(toDate);
		DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern(groupByMonth ? "MM/yyyy" : "dd/MM");
		Map<String, SalesTrendResponse> trendByLabel = new LinkedHashMap<>();

		if (groupByMonth) {
			YearMonth cursor = YearMonth.from(fromDate);
			YearMonth end = YearMonth.from(toDate);
			while (!cursor.isAfter(end)) {
				String label = cursor.format(labelFormatter);
				trendByLabel.put(label, SalesTrendResponse.builder()
						.label(label)
						.revenue(0D)
						.orderCount(0L)
						.build());
				cursor = cursor.plusMonths(1);
			}
		} else {
			LocalDate cursor = fromDate;
			while (!cursor.isAfter(toDate)) {
				String label = cursor.format(labelFormatter);
				trendByLabel.put(label, SalesTrendResponse.builder()
						.label(label)
						.revenue(0D)
						.orderCount(0L)
						.build());
				cursor = cursor.plusDays(1);
			}
		}

		for (SalesTrendResponse item : actualData == null ? List.<SalesTrendResponse>of() : actualData) {
			if (item == null || item.getLabel() == null) {
				continue;
			}
			SalesTrendResponse existing = trendByLabel.get(item.getLabel());
			if (existing == null) {
				trendByLabel.put(item.getLabel(), item);
				continue;
			}
			existing.setRevenue(item.getRevenue() == null ? 0D : item.getRevenue());
			existing.setOrderCount(item.getOrderCount() == null ? 0L : item.getOrderCount());
		}

		return new ArrayList<>(trendByLabel.values());
	}

	private List<OrderStatusReportResponse> fillStatusBreakdownGaps(List<OrderStatusReportResponse> actualData) {
		Map<String, OrderStatusReportResponse> statusByCode = new LinkedHashMap<>();
		for (String status : REPORT_STATUSES) {
			statusByCode.put(status, OrderStatusReportResponse.builder()
					.status(status)
					.orderCount(0L)
					.revenue(0D)
					.build());
		}

		for (OrderStatusReportResponse item : actualData == null ? List.<OrderStatusReportResponse>of() : actualData) {
			if (item == null || item.getStatus() == null || item.getStatus().isBlank()) {
				continue;
			}
			String status = item.getStatus().trim().toUpperCase();
			OrderStatusReportResponse existing = statusByCode.get(status);
			if (existing == null) {
				statusByCode.put(status, item);
				continue;
			}
			existing.setOrderCount(item.getOrderCount() == null ? 0L : item.getOrderCount());
			existing.setRevenue(item.getRevenue() == null ? 0D : item.getRevenue());
		}

		return new ArrayList<>(statusByCode.values());
	}

	private List<MonthlySalesResponse> getMonthlySalesData(int monthCount) {
		int safeMonthCount = Math.max(monthCount, 1);
		YearMonth endMonth = dashboardRepository.findLatestOrderMonth();
		YearMonth startMonth = endMonth.minusMonths(safeMonthCount - 1L);
		DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
		Map<YearMonth, MonthlySalesResponse> monthlyData = new LinkedHashMap<>();

		for (int i = 0; i < safeMonthCount; i++) {
			YearMonth month = startMonth.plusMonths(i);
			monthlyData.put(month, MonthlySalesResponse.builder()
					.label(month.format(labelFormatter))
					.revenue(0D)
					.orderCount(0L)
					.build());
		}

		for (MonthlySalesStat item : dashboardRepository.findMonthlySales(startMonth, endMonth)) {
			MonthlySalesResponse existing = monthlyData.get(item.getMonth());
			if (existing != null) {
				existing.setRevenue(item.getRevenue());
				existing.setOrderCount(item.getOrderCount());
			}
		}

		double maxRevenue = monthlyData.values().stream()
				.mapToDouble(item -> item.getRevenue() == null ? 0D : item.getRevenue())
				.max()
				.orElse(0D);
		long maxOrderCount = monthlyData.values().stream()
				.mapToLong(item -> item.getOrderCount() == null ? 0L : item.getOrderCount())
				.max()
				.orElse(0L);
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

		for (MonthlySalesResponse item : monthlyData.values()) {
			double revenue = item.getRevenue() == null ? 0D : item.getRevenue();
			long orderCount = item.getOrderCount() == null ? 0L : item.getOrderCount();
			item.setRevenuePercent(maxRevenue > 0 ? Math.max(Math.round(revenue * 100 / maxRevenue), 2) : 0);
			item.setOrderPercent(maxOrderCount > 0 ? Math.max(Math.round(orderCount * 100D / maxOrderCount), 2) : 0);
			item.setRevenueLabel(currencyFormat.format(revenue));
		}

		return monthlyData.values().stream().toList();
	}

	private CustomerOverviewResponse getCustomerOverview() {
		long firstTimeCustomers = dashboardRepository.countFirstTimeCustomers();
		long returningCustomers = dashboardRepository.countReturningCustomers();
		long totalBuyingCustomers = firstTimeCustomers + returningCustomers;

		return CustomerOverviewResponse.builder()
				.firstTimeCustomers(firstTimeCustomers)
				.returningCustomers(returningCustomers)
				.totalBuyingCustomers(totalBuyingCustomers)
				.firstTimePercent(totalBuyingCustomers > 0 ? Math.round(firstTimeCustomers * 100D / totalBuyingCustomers) : 0)
				.returningPercent(totalBuyingCustomers > 0 ? Math.round(returningCustomers * 100D / totalBuyingCustomers) : 0)
				.totalOrders(dashboardRepository.countValidOrders())
				.activeProducts(dashboardRepository.countActiveProducts())
				.build();
	}
}
