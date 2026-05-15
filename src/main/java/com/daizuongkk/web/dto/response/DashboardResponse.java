package com.daizuongkk.web.dto.response;

import com.daizuongkk.web.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long totalProducts;
    private Long totalUsers;
    private Long totalOrders;
    private Double totalRevenue;
    private Long pendingOrders;
    private Long completedOrders;
    private List<ProductResponse> lowStockProducts;
    private List<ProductResponse> latestProducts;
    private List<Order> recentOrders;
    private List<MonthlySalesResponse> monthlySalesData;
    private CustomerOverviewResponse customerOverview;
}
