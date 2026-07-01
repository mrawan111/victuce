package com.victusstore.controller;

import com.victusstore.model.Category;
import com.victusstore.repository.CategoryRepository;
import com.victusstore.services.CloudinaryService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EntityManager entityManager;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(category -> ResponseEntity.ok(category))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadCategoryImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "categories") String folder) {
        try {
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, folder);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category image uploaded successfully");
            response.put("image_url", uploadResult.get("url"));
            response.put("cloudinary_public_id", uploadResult.get("public_id"));
            response.put("width", uploadResult.get("width"));
            response.put("height", uploadResult.get("height"));
            response.put("size_bytes", uploadResult.get("bytes"));

            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to upload category image",
                    "details", e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        return categoryRepository.findById(id)
                .map(category -> {
                    category.setCategoryName(categoryDetails.getCategoryName());
                    category.setCategoryImage(categoryDetails.getCategoryImage());
                    category.setIsActive(categoryDetails.getIsActive());
                    Category updatedCategory = categoryRepository.save(category);
                    return ResponseEntity.ok(updatedCategory);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Boolean>> deleteCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    // Remove all product-category relations for this category
                    // This will NOT delete the products, only the join table entries
                    entityManager.createNativeQuery(
                        "DELETE FROM product_categories WHERE category_id = :categoryId"
                    )
                    .setParameter("categoryId", id)
                    .executeUpdate();
                    
                    // Now delete the category
                    categoryRepository.delete(category);
                    
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("deleted", Boolean.TRUE);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
