package com.sha.ecommerce_backend.dto;

import lombok.Data;

@Data
public class GetItemDto {
    private String itemId;
    private String title;
    private String description;
    private String category;
    private Float startingPrice;
    private Float currentPrice;
    private Float buyoutPrice;
    private String condition;
    private String status;
    private String auctionStartTime;
    private String auctionEndTime;
    private String sellerId;
}
