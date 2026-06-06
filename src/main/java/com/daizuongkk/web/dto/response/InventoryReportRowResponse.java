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
public class InventoryReportRowResponse {
    private Long productId;
    private String productName;
    private String category;
    private Long quantity;
    private Double price;
    private String stockStatus;
}
