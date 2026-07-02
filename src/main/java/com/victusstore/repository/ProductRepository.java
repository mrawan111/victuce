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
    
    // Get products from category and all its descendant categories (inheritance)
    @Query(value = "WITH RECURSIVE category_tree AS (" +
           "  SELECT category_id FROM categories WHERE category_id = :categoryId " +
           "  UNION ALL " +
           "  SELECT c.category_id FROM categories c " +
           "  INNER JOIN category_tree ct ON c.parent_category_id = ct.category_id" +
           ") " +
           "SELECT DISTINCT p.* FROM products p " +
           "INNER JOIN product_categories pc ON p.product_id = pc.product_id " +
           "INNER JOIN category_tree ct ON pc.category_id = ct.category_id",
           nativeQuery = true)
    List<Product> findByCategoryIdWithInheritance(@Param("categoryId") Long categoryId);
    
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByIsActiveTrue();
}
