package com.daizuongkk.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemResponse {
	private Long id;
	private ProductResponse product;
	private Long cartId;
	private Long quantity;

}
