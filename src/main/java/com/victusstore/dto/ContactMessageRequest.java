package com.victusstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactMessageRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be 255 characters or less")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 255, message = "email must be 255 characters or less")
    private String email;

    @NotBlank(message = "subject is required")
    @Size(max = 255, message = "subject must be 255 characters or less")
    private String subject;

    @NotBlank(message = "message is required")
    @Size(max = 5000, message = "message must be 5000 characters or less")
    private String message;
}
