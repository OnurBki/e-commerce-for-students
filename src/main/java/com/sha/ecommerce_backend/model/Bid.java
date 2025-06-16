package com.sha.ecommerce_backend.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Bid {
    @NotBlank(message = "Bid ID is required")
    @Size(max = 255, message = "Bid ID must be at most 255 characters long")
    private String bidId;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.0", message = "Bid amount must be at least 0.00")
    @DecimalMax(value = "999999.99", message = "Bid amount must be at most 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Bid amount must be a valid number with at most two decimal places")
    private Float bidAmount;

    @NotBlank(message = "Bid time is required")
    @FutureOrPresent(message = "Bid time must be in the present or future")
    private LocalDateTime bidTime = LocalDateTime.now();

    @NotBlank(message = "Item ID is required")
    @Size(max = 255, message = "Item ID must be at most 255 characters long")
    private String itemId;

    @NotBlank(message = "Bidder ID is required")
    @Size(max = 255, message = "Bidder ID must be at most 255 characters long")
    private String bidderId;
}