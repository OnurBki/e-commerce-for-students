package com.sha.ecommerce_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateItemDto {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters long")
    private String title;

    @NotBlank(message = "Price is required")
    @Size(max = 500, message = "Description must be at most 500 characters long")
    private String description = "";

    @NotNull(message = "Category is required")
    @Min(value = 0, message = "Category must be a valid category ID")
    @Max(value = 9, message = "Category must be a valid category ID")
    private int category;

    @NotNull(message = "Starting price is required")
    @DecimalMin(value = "0.0", message = "Starting price must be at least 0.00")
    @DecimalMax(value = "100000.00", message = "Starting price must be at most 100000.00")
    @Digits(integer = 6, fraction = 2, message = "Starting price must be a valid number with at most two decimal places")
    private Float startingPrice;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.0", message = "Current price must be at least 0.00")
    @DecimalMax(value = "999999.99", message = "Current price must be at most 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Current price must be a valid number with at most two decimal places")
    private Float currentPrice;

    @NotNull(message = "buyOutPrice is required")
    @DecimalMin(value = "0.0", message = "Buy out price must be at least 0.00")
    @DecimalMax(value = "999999.99", message = "Buy out price must be at most 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Buy out price must be a valid number with at most two decimal places")
    private Float buyoutPrice;

    @NotNull(message = "Condition is required")
    @Min(value = 0, message = "Condition must be a valid condition ID")
    @Max(value = 4, message = "Condition must be a valid condition ID")
    private int condition;

    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Status must be a valid status ID")
    @Max(value = 4, message = "Status must be a valid status ID")
    private int status = 0; // Default to 'active'

    @NotBlank(message = "Auction start time is required")
    @FutureOrPresent(message = "Auction start time must be in the present")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auctionStartTime = LocalDateTime.now();

    @NotBlank(message = "Auction end time is required")
    @Future(message = "Auction end time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auctionEndTime;

    @NotBlank(message = "Seller ID is required")
    @Size(max = 255, message = "Seller ID must be at most 255 characters long")
    private String sellerId;
}
