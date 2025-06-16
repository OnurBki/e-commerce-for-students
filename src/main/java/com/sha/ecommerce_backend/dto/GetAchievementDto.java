package com.sha.ecommerce_backend.dto;

import lombok.Data;

@Data
public class GetAchievementDto {
    private String achievementId;
    private String title;
    private String description;
    private Float awardAmount;
}
