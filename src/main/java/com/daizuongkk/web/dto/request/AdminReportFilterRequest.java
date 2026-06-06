package com.daizuongkk.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportFilterRequest {
    private String reportType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;
    private String paymentMethod;
    private Integer topLimit;
    private String exportedBy;
}
