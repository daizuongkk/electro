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
public class MonthlySalesResponse {
    private String label;
    private Double revenue;
    private Long orderCount;
    private Long revenuePercent;
    private Long orderPercent;
    private String revenueLabel;
}
