package com.victusstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrderRequest {
    
    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNum;
    
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;
    
    @Size(max = 20, message = "Order status must not exceed 20 characters")
    private String orderStatus;
    
    @Size(max = 20, message = "Payment status must not exceed 20 characters")
    private String paymentStatus;
    
    private Boolean clearCart;
}

