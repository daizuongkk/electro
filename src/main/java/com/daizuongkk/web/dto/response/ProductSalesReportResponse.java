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
public class ProductSalesReportResponse {
    private LocalDateTime generatedAt;
    private Integer topLimit;
    private Long totalQuantitySold;
    private Double totalRevenue;
    private List<ProductSalesReportRowResponse> rows;
}
