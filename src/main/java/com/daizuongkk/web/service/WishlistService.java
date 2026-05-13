package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.ProductResponse;
import com.daizuongkk.web.model.Product;
import com.daizuongkk.web.repository.WishlistRepository;

import java.util.ArrayList;
import java.util.List;

public class WishlistService {

	private final WishlistRepository wishlistRepository = new WishlistRepository();
	private final ProductService productService = new ProductService();

	public List<Long> getWishlistProductIds(Long userId) {
		return wishlistRepository.findProductIdsByUserId(userId);
	}

	public List<ProductResponse> getWishlistProducts(Long userId) {
		List<Long> productIds = wishlistRepository.findProductIdsByUserId(userId);
		List<ProductResponse> products = new ArrayList<>();

		for (Long productId : productIds) {
			ProductResponse product = productService.getProductById(productId);
			if (product != null) {
				products.add(product);
			}
		}

		return products;
	}

	public boolean toggleWishlist(Long userId, Long productId) {
		if (userId == null || userId <= 0 || productId == null || productId <= 0) {
			return false;
		}

		if (wishlistRepository.exists(userId, productId)) {
			return wishlistRepository.delete(userId, productId);
		}

		return wishlistRepository.save(userId, productId);
	}

	public boolean removeFromWishlist(Long userId, Long productId) {
		return wishlistRepository.delete(userId, productId);
	}

	public boolean isWishlisted(Long userId, Long productId) {
		return wishlistRepository.exists(userId, productId);
	}
}

