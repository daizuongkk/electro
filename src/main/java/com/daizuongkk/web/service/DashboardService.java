package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.request.AdminUserSearchRequest;
import com.daizuongkk.web.dto.response.CustomerOverviewResponse;
import com.daizuongkk.web.dto.response.DashboardResponse;
import com.daizuongkk.web.dto.response.MonthlySalesResponse;
import com.daizuongkk.web.model.MonthlySalesStat;
import com.daizuongkk.web.repository.DashboardRepository;

import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final ProductService productService;
    private final UserService userService;

    public DashboardService() {
        this(new DashboardRepository(), new ProductService(), new UserService());
    }

    public DashboardService(DashboardRepository dashboardRepository, ProductService productService, UserService userService) {
        this.dashboardRepository = dashboardRepository != null ? dashboardRepository : new DashboardRepository();
        this.productService = productService != null ? productService : new ProductService();
        this.userService = userService != null ? userService : new UserService();
    }

    public DashboardResponse getDashboard() {
        return DashboardResponse.builder()
                .totalProducts(productService.countProducts())
                .totalUsers(userService.countUsers(AdminUserSearchRequest.builder().build()))
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
