package com.daizuongkk.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    private LocalDateTime generatedAt;
    private Long totalOrders;
    private Long totalProductsSold;
    private Double totalRevenue;
    private Double averageOrderValue;
    private List<RevenueReportRowResponse> rows;
}
