package com.daizuongkk.web.dto.response;

import com.daizuongkk.web.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime generatedAt;
    private Long totalOrders;
    private Long validOrders;
    private Double validRevenue;
    private Long pendingOrders;
    private Long paidOrders;
    private Long shippedOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long uniqueCustomers;
    private Long itemsSold;
    private Double averageOrderValue;
    private List<SalesTrendResponse> salesTrend;
    private List<OrderStatusReportResponse> statusBreakdown;
    private List<TopProductReportResponse> topProducts;
    private List<Order> recentOrders;
    private List<Order> orders;
}
