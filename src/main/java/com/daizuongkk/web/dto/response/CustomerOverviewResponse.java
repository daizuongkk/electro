package com.daizuongkk.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOverviewResponse {
    private Long firstTimeCustomers;
    private Long returningCustomers;
    private Long totalBuyingCustomers;
    private Long firstTimePercent;
    private Long returningPercent;
    private Long totalOrders;
    private Long activeProducts;
}
