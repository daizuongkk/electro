package com.daizuongkk.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private Double totalPrice;
    private String status;
    private String recipientName;
    private String phone;
    private String address;
    private Date createdAt;
    private List<OrderItem> items;
}
