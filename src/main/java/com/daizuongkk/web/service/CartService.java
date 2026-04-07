package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.model.Cart;
import com.daizuongkk.web.model.CartItem;
import com.daizuongkk.web.model.User;
import com.daizuongkk.web.repository.CartRepository;
import com.daizuongkk.web.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class CartService {

    private CartRepository cartRepository = new CartRepository();
    private  ProductService productService = new ProductService();
    private UserRepository userRepository = new UserRepository();
    public List<CartItemResponse> getCart(Long userId) {



        List<CartItem>  cartItems = cartRepository.findItemsByUserId(userId);

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


    public void addToCart(Long id, Long productId) {

        Cart cart = cartRepository.findByUserId(id);

//        CartItem cartItem = c




        CartItem cartItem = CartItem.builder().cartId(cart.getId()).productId(productId).quantity(1L).build();

    }
}
