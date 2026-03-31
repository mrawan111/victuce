package com.victusstore.repository;

import com.victusstore.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    Optional<Review> findByProductIdAndEmail(Long productId, String email);

    long countByProductId(Long productId);

    @Query("select avg(r.rating) from Review r where r.productId = :productId")
    Double findAverageRatingByProductId(Long productId);
}
