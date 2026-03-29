package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.response.ReviewResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.model.Review;
import com.daizuongkk.web.repository.ReviewRepository;
import com.daizuongkk.web.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReviewService {
    private final ReviewRepository reviewRepository;
	private final UserService userService;

    public ReviewService() {
        this.userService = new UserService();
        this.reviewRepository = new ReviewRepository();
    }


    public ReviewService(ReviewRepository reviewRepository, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
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

            reviewResponse.setUserDisplayName(userResponse.getFirstName() + " " + userResponse.getLastName());
            reviewResponse.setFiveStars((int) reviews.stream().filter(r -> r.getScore() == 5).count());
            reviewResponse.setFourStars((int) reviews.stream().filter(r -> r.getScore() == 4).count());
            reviewResponse.setThreeStars((int) reviews.stream().filter(r -> r.getScore() == 3).count());
            reviewResponse.setTwoStars((int) reviews.stream().filter(r -> r.getScore() == 2).count());
            reviewResponse.setOneStars((int) reviews.stream().filter(r -> r.getScore() == 1).count());
            reviewResponses.add(reviewResponse);
        }
        return reviewResponses;
    }

    public boolean addReview(Long productId, Long userId, String message, int score) {
        if (productId == null || productId <= 0 || userId == null || userId <= 0) {
            return false;
        }

        if (score < 1 || score > 5) {
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


    private ReviewResponse reviewToResponse(Review review) {


        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .message(review.getMessage())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
