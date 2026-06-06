package com.daizuongkk.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportRowResponse {
    private Long orderId;
    private String customerName;
    private Date createdAt;
    private Double totalAmount;
    private String status;
    private String statusLabel;
    private String paymentMethod;
}
