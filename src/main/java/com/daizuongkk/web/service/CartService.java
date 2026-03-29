package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.model.CartItem;
import com.daizuongkk.web.repository.CartRepository;

import java.util.ArrayList;
import java.util.List;

public class CartService {

    private CartRepository cartRepository = new CartRepository();
    private  ProductService productService = new ProductService();

    public List<CartItemResponse> getCart(Long userId) {



        List<CartItem>  cartItems = cartRepository.findByUserId(userId);

        List<CartItemResponse> cartItemResponses = new ArrayList<>();
        for (CartItem cartItem : cartItems) {



                CartItemResponse cartItemResponse = CartItemResponse.builder()
                        .id(cartItem.getId())
                        .product(productService.getProductById(cartItem.getProductId()))
                        .quantity(cartItem.getQuantity())
                        .build();
                cartItemResponses.add(cartItemResponse);
        }
        return cartItemResponses;
    }


}
