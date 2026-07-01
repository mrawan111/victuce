package com.victusstore.repository;

import com.victusstore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByIsActiveTrue();
}
