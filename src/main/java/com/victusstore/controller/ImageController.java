package com.victusstore.controller;

import com.victusstore.model.Image;
import com.victusstore.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping
    public ResponseEntity<List<Image>> getAllImages() {
        List<Image> images = imageRepository.findAll();
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImageById(@PathVariable Long id) {
        Optional<Image> image = imageRepository.findById(id);
        if (image.isPresent()) {
            return ResponseEntity.ok(image.get());
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Image not found"));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Image>> getImagesByProductId(@PathVariable Long productId) {
        List<Image> images = imageRepository.findByProductId(productId);
        return ResponseEntity.ok(images);
    }

    @PostMapping
    public ResponseEntity<?> createImage(@RequestBody Image image) {
        try {
            if (image.getProductId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Product ID is required"));
            }
            if (image.getImageUrl() == null || image.getImageUrl().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Image URL is required"));
            }

            Image savedImage = imageRepository.save(image);
            return ResponseEntity.status(201).body(savedImage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateImage(@PathVariable Long id, @RequestBody Image imageDetails) {
        try {
            Optional<Image> imageOpt = imageRepository.findById(id);
            if (imageOpt.isPresent()) {
                Image image = imageOpt.get();
                if (imageDetails.getImageUrl() != null) {
                    image.setImageUrl(imageDetails.getImageUrl());
                }
                if (imageDetails.getIsPrimary() != null) {
                    // If setting this as primary, unset other primary images for the same product
                    if (imageDetails.getIsPrimary()) {
                        List<Image> productImages = imageRepository.findByProductId(image.getProductId());
                        for (Image img : productImages) {
                            if (!img.getImageId().equals(id)) {
                                img.setIsPrimary(false);
                                imageRepository.save(img);
                            }
                        }
                    }
                    image.setIsPrimary(imageDetails.getIsPrimary());
                }
                if (imageDetails.getVariantId() != null) {
                    image.setVariantId(imageDetails.getVariantId());
                }
                Image updatedImage = imageRepository.save(image);
                return ResponseEntity.ok(updatedImage);
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Image not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        try {
            if (imageRepository.existsById(id)) {
                imageRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Image not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

