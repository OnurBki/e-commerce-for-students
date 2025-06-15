package com.sha.ecommerce_backend.dto;

import lombok.Data;

@Data
public class GetBidDto {
    private String bidId;
    private Float bidAmount;
    private String bidTime;
    private String itemId;
    private String bidderId;
}
