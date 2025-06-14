package com.sha.ecommerce_backend.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class User {
    @NotBlank(message = "ID is required")
    @Size(max = 255, message = "ID must be at most 255 characters long")
    private String id;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must be at most 100 characters long")
    private String userName;

    @NotBlank(message = "Password is required")
    private String hashedPassword;

    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must be at most 255 characters long")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @Nullable
    private String studentId;

    @NotBlank(message = "Reputation is required")
    @Min(value = 0, message = "Reputation must be at least 0")
    @Max(value = 5, message = "Reputation must be at most 5")
    @Digits(integer = 1, fraction = 1, message = "Reputation must be a valid number with at most one decimal place")
    private Float reputation = 0.0f; // Default reputation is 0.0

    @NotBlank(message = "Balance is required")
    @Min(value = 0, message = "Balance must be at least 0")
    @Max(value = 999999, message = "Balance must be at most 999999")
    @Digits(integer = 6, fraction = 2, message = "Balance must be a valid number with at most two decimal places")
    private Float balance = 100.0f; // Default balance is 100.0 (student friendly platform :))

    @NotBlank(message = "Admin status is required")
    private Boolean isAdmin = false;
}
