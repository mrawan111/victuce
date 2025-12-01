package com.victusstore.controller;

import com.victusstore.model.Image;
import com.victusstore.repository.ImageRepository;
import com.victusstore.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

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

    /**
     * ⭐ NEW: Upload single image to Cloudinary and save to database
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("product_id") Long productId,
            @RequestParam(value = "variant_id", required = false) Long variantId,
            @RequestParam(value = "is_primary", defaultValue = "false") Boolean isPrimary,
            @RequestParam(value = "folder", defaultValue = "products") String folder) {
        try {
            // Validate product_id
            if (productId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Product ID is required"));
            }

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, folder);
            String imageUrl = uploadResult.get("url").toString();

            // Save to database
            Image image = Image.builder()
                    .productId(productId)
                    .variantId(variantId)
                    .imageUrl(imageUrl)
                    .isPrimary(isPrimary)
                    .build();

            // If setting as primary, unset other primary images for the same product
            if (isPrimary) {
                List<Image> productImages = imageRepository.findByProductId(productId);
                for (Image img : productImages) {
                    img.setIsPrimary(false);
                    imageRepository.save(img);
                }
            }

            Image savedImage = imageRepository.save(image);

            // Return response with both upload and database info
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("image_id", savedImage.getImageId());
            response.put("image_url", savedImage.getImageUrl());
            response.put("cloudinary_public_id", uploadResult.get("public_id"));
            response.put("width", uploadResult.get("width"));
            response.put("height", uploadResult.get("height"));
            response.put("size_bytes", uploadResult.get("bytes"));

            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to upload image",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * ⭐ NEW: Upload multiple images at once
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("product_id") Long productId,
            @RequestParam(value = "variant_id", required = false) Long variantId,
            @RequestParam(value = "folder", defaultValue = "products") String folder) {
        try {
            if (productId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Product ID is required"));
            }

            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "No files provided"));
            }

            // Upload all files to Cloudinary
            Map<String, Object>[] uploadResults = cloudinaryService.uploadMultipleImages(files, folder);

            // Save all to database
            List<Map<String, Object>> savedImages = new ArrayList<>();
            for (int i = 0; i < uploadResults.length; i++) {
                String imageUrl = uploadResults[i].get("url").toString();
                
                Image image = Image.builder()
                        .productId(productId)
                        .variantId(variantId)
                        .imageUrl(imageUrl)
                        .isPrimary(i == 0) // First image is primary by default
                        .build();

                Image savedImage = imageRepository.save(image);

                Map<String, Object> imageInfo = new HashMap<>();
                imageInfo.put("image_id", savedImage.getImageId());
                imageInfo.put("image_url", savedImage.getImageUrl());
                imageInfo.put("is_primary", savedImage.getIsPrimary());
                imageInfo.put("cloudinary_public_id", uploadResults[i].get("public_id"));

                savedImages.add(imageInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", files.length + " images uploaded successfully");
            response.put("uploaded_count", files.length);
            response.put("images", savedImages);

            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to upload images",
                    "details", e.getMessage()
            ));
        }
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
