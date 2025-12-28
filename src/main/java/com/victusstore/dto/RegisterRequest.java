package com.victusstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @Size(max = 255, message = "First name must not exceed 255 characters")
    private String firstName;
    
    @Size(max = 255, message = "Last name must not exceed 255 characters")
    private String lastName;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must contain 10-15 digits")
    private String phoneNum;
    
    private Boolean sellerAccount;
}

