package com.sha.ecommerce_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAchievementDto {
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Award amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Award amount must be greater than 0")
    @DecimalMax(value = "1000.0", message = "Award amount cannot exceed 1,000.0")
    @Digits(integer = 4, fraction = 2, message = "Award amount must be a valid decimal with up to 2 decimal places")
    private Float awardAmount;
}
