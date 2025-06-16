package com.sha.ecommerce_backend.service;

import com.sha.ecommerce_backend.dto.CreateBidDto;
import com.sha.ecommerce_backend.dto.GetBidDto;
import com.sha.ecommerce_backend.repository.BidRepository;
import com.sha.ecommerce_backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BidService {
    @Autowired private BidRepository bidRepository;
    @Autowired private JwtUtils jwtUtils;

    public List<GetBidDto> getBidsByItemId(String itemId) {
        try {
            return bidRepository.getBidsByItemId(itemId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve bids for item: " + itemId, e);
        }
    }

    public List<GetBidDto> getBidsByBidderId(String bidderId) {
        try {
            return bidRepository.getBidsByBidderId(bidderId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve bids for bidder: " + bidderId, e);
        }
    }

    public void saveBid(CreateBidDto createBidDto, String token) {
        try {
            String bidderId = jwtUtils.extractUserId(token);
            if (!bidderId.equals(createBidDto.getBidderId())) {
                throw new IllegalArgumentException("Bidder ID from token does not match the provided bidder ID");
            }
            bidRepository.save(createBidDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save bid: ", e);
        }
    }

    public void buyOutItem(String itemId, String token) {
        try {
            String bidderId = jwtUtils.extractUserId(token);
            bidRepository.buyOut(itemId, bidderId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to buy out item: " + itemId, e);
        }
    }

    @Scheduled(fixedRate = 10000) // Runs every 10 seconds
    public void finalizeAuctions() {
        try {
            bidRepository.finalizeAuctions();
        } catch (Exception e) {
            throw new RuntimeException("Failed to finalize auctions", e);
        }
    }
}
