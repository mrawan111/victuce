package com.victusstore.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary
     * @param file The image file to upload
     * @param folder Optional folder name in Cloudinary (e.g., "products", "categories")
     * @return Map containing upload result with 'url' and 'public_id'
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size must not exceed 10MB");
        }

        // Generate unique public ID
        String publicId = UUID.randomUUID().toString();

        // Upload parameters
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder != null ? folder : "victusstore",
                "resource_type", "image",
                "overwrite", false,
                "use_filename", false,
                "unique_filename", true
        );

        try {
            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            // Return relevant information
            return Map.of(
                    "url", uploadResult.get("secure_url").toString(),
                    "public_id", uploadResult.get("public_id").toString(),
                    "format", uploadResult.get("format").toString(),
                    "width", uploadResult.get("width"),
                    "height", uploadResult.get("height"),
                    "bytes", uploadResult.get("bytes")
            );
        } catch (IOException e) {
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Delete image from Cloudinary
     * @param publicId The public ID of the image to delete
     * @return Map containing deletion result
     * @throws IOException if deletion fails
     */
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return Map.of(
                    "result", result.get("result").toString(),
                    "success", "ok".equals(result.get("result").toString())
            );
        } catch (IOException e) {
            throw new IOException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Upload multiple images
     * @param files Array of image files
     * @param folder Optional folder name
     * @return Array of upload results
     */
    public Map<String, Object>[] uploadMultipleImages(MultipartFile[] files, String folder) throws IOException {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files provided");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object>[] results = new Map[files.length];
        
        for (int i = 0; i < files.length; i++) {
            results[i] = uploadImage(files[i], folder);
        }
        
        return results;
    }
}