package com.daizuongkk.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.daizuongkk.web.dto.response.CartItemResponse;
import com.daizuongkk.web.model.Cart;
import com.daizuongkk.web.model.CartItem;
import com.daizuongkk.web.model.Product;
import com.daizuongkk.web.repository.CartItemRepository;
import com.daizuongkk.web.repository.CartRepository;
import com.daizuongkk.web.repository.ProductRepository;

public class CartService {

	private CartRepository cartRepository = new CartRepository();
	private ProductService productService = new ProductService();
	private CartItemRepository cartItemRepository = new CartItemRepository();
	private ProductRepository productRepository = new ProductRepository();

	public List<CartItemResponse> getCartItems(Long userId) {

		List<CartItem> cartItems = cartRepository.findItemsByUserId(userId);

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

	public List<CartItemResponse> getCartItems(Long userId, Set<Long> productIds) {
		List<CartItemResponse> cartItems = getCartItems(userId);
		if (productIds == null || productIds.isEmpty()) {
			return new ArrayList<>();
		}

		return cartItems.stream()
				.filter(item -> item.getProduct() != null && productIds.contains(item.getProduct().getId()))
				.toList();
	}

	public boolean addToCart(Long id, Long productId, Long qty) {

		if (id == null) {
			return false;
		}

		Cart cart = cartRepository.findOrCreateByUserId(id);

		if (cart == null) {
			return false;
		}

		if (productId == null) {
			return false;
		}

		Product product = productRepository.findById(productId);

		if (product == null) {
			return false;
		}

		List<CartItem> cartItems = cartRepository.findItemsByUserId(id);
		for (CartItem cartItem : cartItems) {
			if (cartItem.getProductId().equals(productId)) {
				cartItem.setQuantity(cartItem.getQuantity() + (qty == null ? 1 : qty));
				cartItemRepository.update(cartItem);
				return true;
			}

		}

		CartItem cartItem = CartItem.builder()
				.cartId(cart.getId())
				.productId(productId)
				.quantity(qty == null ? 1 : qty)
				.build();

		cartItemRepository.save(cartItem);

		return true;

	}

	public void delete(String productIds, Long userId) {

		cartItemRepository.delete(productIds, userId);

	}

	public boolean updateQuantity(Long userId, Long productId, Long quantity) {
		if (userId == null || productId == null || quantity == null || quantity <= 0) {
			return false;
		}

		return cartItemRepository.updateQuantityByUserAndProduct(userId, productId, quantity);
	}
}
