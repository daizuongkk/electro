package com.daizuongkk.web.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminProductSearchRequest {
    private String keyword;
    private String category;
    private String brand;
    private Double minPrice;
    private Double maxPrice;
    private Long minQuantity;
    private Long maxQuantity;
    private String deleted;
    private String sortBy;
}
