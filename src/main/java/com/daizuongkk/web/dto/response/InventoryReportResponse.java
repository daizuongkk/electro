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
public class InventoryReportResponse {
    private LocalDateTime generatedAt;
    private Long totalProducts;
    private Long outOfStockProducts;
    private Long lowStockProducts;
    private Long inStockProducts;
    private Double inventoryValue;
    private List<InventoryReportRowResponse> rows;
}
