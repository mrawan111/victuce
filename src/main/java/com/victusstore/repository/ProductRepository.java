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
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    // Get products from this category AND all ancestor (parent) categories (upward inheritance).
    // When browsing Category B (child of A), you also see all products directly in Category A.
    @Query(value = "WITH RECURSIVE category_tree AS (" +
           "  SELECT category_id, parent_category_id FROM categories WHERE category_id = :categoryId " +
           "  UNION ALL " +
           "  SELECT c.category_id, c.parent_category_id FROM categories c " +
           "  INNER JOIN category_tree ct ON c.category_id = ct.parent_category_id" +
           ") " +
           "SELECT DISTINCT p.product_id FROM products p " +
           "INNER JOIN category_tree ct ON p.category_id = ct.category_id",
           nativeQuery = true)
    List<Long> findProductIdsByCategoryIdWithInheritance(@Param("categoryId") Long categoryId);
    
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByIsActiveTrue();
}
