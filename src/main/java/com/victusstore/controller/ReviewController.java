package com.victusstore.controller;

import com.victusstore.dto.CreateReviewRequest;
import com.victusstore.dto.ProductReviewsResponse;
import com.victusstore.dto.ReviewResponse;
import com.victusstore.model.Account;
import com.victusstore.model.Product;
import com.victusstore.model.Review;
import com.victusstore.repository.AccountRepository;
import com.victusstore.repository.ProductRepository;
import com.victusstore.repository.ReviewRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/api/reviews/product/{productId}")
    public ResponseEntity<ProductReviewsResponse> getReviewsByProduct(@PathVariable Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOptional.get();
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        return ResponseEntity.ok(ProductReviewsResponse.builder()
                .productId(productId)
                .averageRating(normalizeRating(product.getProductRating()))
                .reviewCount(reviews.size())
                .reviews(reviews.stream().map(this::toResponse).toList())
                .build());
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<?> createOrUpdateReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        Optional<Product> productOptional = productRepository.findById(request.getProductId());
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Product not found"));
        }

        String email = authentication.getName().trim().toLowerCase();
        Account account = accountRepository.findByEmail(email).orElse(null);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Account not found"));
        }

        Review review = reviewRepository.findByProductIdAndEmail(request.getProductId(), email)
                .orElseGet(Review::new);

        review.setProductId(request.getProductId());
        review.setEmail(email);
        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));

        Review savedReview = reviewRepository.save(review);
        refreshProductRating(request.getProductId());

        return ResponseEntity.ok(toResponse(savedReview));
    }

    @GetMapping("/api/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        List<ReviewResponse> reviews = reviewRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/api/admin/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        Optional<Review> reviewOptional = reviewRepository.findById(id);
        if (reviewOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Long productId = reviewOptional.get().getProductId();
        reviewRepository.deleteById(id);
        refreshProductRating(productId);

        return ResponseEntity.ok(Map.of("deleted", true));
    }

    private ReviewResponse toResponse(Review review) {
        String reviewerName = review.getEmail();

        if (review.getAccount() != null) {
            String firstName = review.getAccount().getFirstName();
            String lastName = review.getAccount().getLastName();
            String fullName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
            if (!fullName.isEmpty()) {
                reviewerName = fullName;
            }
        }

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProductId())
                .productName(review.getProduct() != null ? review.getProduct().getProductName() : null)
                .email(review.getEmail())
                .reviewerName(reviewerName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private void refreshProductRating(Long productId) {
        productRepository.findById(productId).ifPresent(product -> {
            Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
            product.setProductRating(normalizeRating(averageRating == null
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(averageRating)));
            productRepository.save(product);
        });
    }

    private BigDecimal normalizeRating(BigDecimal rating) {
        if (rating == null) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return rating.setScale(1, RoundingMode.HALF_UP);
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
