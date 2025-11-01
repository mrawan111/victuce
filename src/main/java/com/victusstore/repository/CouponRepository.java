package com.victusstore.repository;

import com.victusstore.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCouponCode(String couponCode);
    List<Coupon> findByIsActive(Boolean isActive);
    List<Coupon> findByIsActiveAndValidFromLessThanEqualAndValidUntilGreaterThanEqual(
            Boolean isActive, LocalDateTime now1, LocalDateTime now2);
}

