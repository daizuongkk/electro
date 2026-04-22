package com.daizuongkk.web.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Builder
public class CartItem {
    private Long id;
    private Long productId;
    private Long cartId;
    private Long quantity;
    private Date createdAt;
    private Date updatedAt;
}
