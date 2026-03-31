package com.victusstore.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductReviewsResponse {
    private Long productId;
    private BigDecimal averageRating;
    private long reviewCount;
    private List<ReviewResponse> reviews;
}
