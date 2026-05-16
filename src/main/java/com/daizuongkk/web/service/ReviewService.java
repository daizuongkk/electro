package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.request.AdminReviewSearchRequest;
import com.daizuongkk.web.dto.response.ReviewResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Review;
import com.daizuongkk.web.repository.OrderRepository;
import com.daizuongkk.web.repository.ReviewRepository;

import java.util.*;

public class ReviewService {
    private final ReviewRepository reviewRepository;
	private final UserService userService;
    private final OrderRepository orderRepository;

    public ReviewService() {
        this.userService = new UserService();
        this.reviewRepository = new ReviewRepository();
        this.orderRepository = new OrderRepository();
    }


    public ReviewService(ReviewRepository reviewRepository, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.orderRepository = new OrderRepository();
    }

    public List<ReviewResponse> getReviewsByProductId(Long productId, int page, int size) {
        if (productId == null || productId <= 0) {
            return Collections.emptyList();
        }

        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.max(size, 1);
        List<Review> reviews = reviewRepository.findByProductId(productId, normalizedPage, normalizedSize);

        List<ReviewResponse> reviewResponses = new ArrayList<ReviewResponse>();
        for (Review review : reviews) {

            UserResponse userResponse = userService.findById(review.getUserId());
            ReviewResponse reviewResponse = reviewToResponse(review);

            if (userResponse != null) {
                reviewResponse.setUserDisplayName((userResponse.getFirstName() + " " + userResponse.getLastName()).trim());
            }
            reviewResponses.add(reviewResponse);
        }
        return reviewResponses;
    }

    public Map<String, Long> countStars(Long productId) {
        if (productId == null || productId <= 0) {
            return Collections.emptyMap();
        }

        Map<String, Long> counts = new HashMap<>();
        counts.put("oneStars", reviewRepository.countByScore(productId, 1));
        counts.put("twoStars", reviewRepository.countByScore(productId, 2));
        counts.put("threeStars", reviewRepository.countByScore(productId, 3));
        counts.put("fourStars", reviewRepository.countByScore(productId, 4));
        counts.put("fiveStars", reviewRepository.countByScore(productId, 5));
        return counts;

    }


    public boolean addReview(Long productId, Long userId, String message, int score) {
        if (productId == null || productId <= 0 || userId == null || userId <= 0) {
            return false;
        }

        if (score < 1 || score > 5) {
            return false;
        }

        if (message == null || message.trim().isEmpty() || message.trim().length() > 2000) {
            return false;
        }

        if (!canUserReviewProduct(productId, userId)) {
            return false;
        }

        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            return false;
        }

        return reviewRepository.create(productId, userId, message, score);
    }

    public long countReviewsByProductId(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    public double getAverageScoreByProductId(Long productId) {
        return reviewRepository.findAverageScoreByProductId(productId);
    }

    public boolean hasUserReviewedProduct(Long productId, Long userId) {
        return reviewRepository.existsByProductIdAndUserId(productId, userId);
    }

    public boolean canUserReviewProduct(Long productId, Long userId) {
        return orderRepository.hasCompletedOrderContainingProduct(userId, productId);
    }

    public List<ReviewResponse> getAdminReviews(AdminReviewSearchRequest filters, int page, int size) {
        return reviewRepository.findAdminPage(filters, page, size)
                .stream()
                .map(this::reviewToResponse)
                .toList();
    }

    public long countAdminReviews(AdminReviewSearchRequest filters) {
        return reviewRepository.countAdmin(filters);
    }

    public boolean deleteReview(Long id) {
        return reviewRepository.deleteById(id);
    }


    private ReviewResponse reviewToResponse(Review review) {


        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .productName(review.getProductName())
                .userDisplayName(review.getUserDisplayName())
                .userEmail(review.getUserEmail())
                .score(review.getScore())
                .message(review.getMessage())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
