package com.daizuongkk.web.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItem {
    private Long id;
    private Long productId;
    private Long cartId;
    private Long quantity;
}
