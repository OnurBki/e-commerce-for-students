package com.sha.ecommerce_backend.controller;

import com.sha.ecommerce_backend.dto.CreateBidDto;
import com.sha.ecommerce_backend.dto.GetBidDto;
import com.sha.ecommerce_backend.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
public class BidController {
    @Autowired private BidService bidService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("by-item/{itemId}")
    public List<GetBidDto> getBidsByItemId(@PathVariable String itemId) {
        try {
            return bidService.getBidsByItemId(itemId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve bids for item: " + itemId, e);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("by-bidder/{bidderId}")
    public List<GetBidDto> getBidsByBidderId(@PathVariable String bidderId) {
        try {
            return bidService.getBidsByBidderId(bidderId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve bids for bidder: " + bidderId, e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("save")
    public void saveBid(
            @RequestBody CreateBidDto createBidDto,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            bidService.saveBid(createBidDto, token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save bid: ", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("buy-out/{itemId}")
    public void buyOutItem(
            @PathVariable String itemId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            bidService.buyOutItem(itemId, token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to buy out item: " + itemId, e);
        }
    }
}
