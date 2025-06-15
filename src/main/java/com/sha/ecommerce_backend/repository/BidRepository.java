package com.sha.ecommerce_backend.repository;

import com.sha.ecommerce_backend.dto.CreateBidDto;
import com.sha.ecommerce_backend.dto.GetBidDto;
import com.sha.ecommerce_backend.mapper.BidMapper;
import com.sha.ecommerce_backend.util.StringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BidRepository {
    @Autowired private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired private BidMapper bidMapper;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    public List<GetBidDto> getBidsByItemId(String itemId) {
        String cacheKey = "bids:item:" + itemId;
        List<GetBidDto> bids = (List<GetBidDto>) redisTemplate.opsForValue().get(cacheKey);
        if (bids != null) {
            return bids;
        }

        String sql = "SELECT * FROM bid WHERE item_id = :itemId ORDER BY bid_time DESC";
        Map<String, Object> params = Map.of("itemId", itemId);
        bids = namedParameterJdbcTemplate.query(sql, params, bidMapper.getBidRowMapper());

        if (!bids.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, bids);
        }

        return bids;
    }

    public List<GetBidDto> getBidsByBidderId(String bidderId) {
        String cacheKey = "bids:bidder:" + bidderId;
        List<GetBidDto> bids = (List<GetBidDto>) redisTemplate.opsForValue().get(cacheKey);
        if (bids != null) {
            return bids;
        }

        String sql = "SELECT * FROM bid WHERE user_id = :bidderId ORDER BY bid_time DESC";
        Map<String, Object> params = Map.of("bidderId", bidderId);
        bids = namedParameterJdbcTemplate.query(sql, params, bidMapper.getBidRowMapper());

        if (!bids.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, bids);
        }

        return bids;
    }

    public void save(CreateBidDto createBidDto) {
        String bidId = StringGenerator.generateRandomString(255);
        while (bidExists(bidId)) {
            bidId = StringGenerator.generateRandomString(255);
        }

        String sql = "CALL place_bid(:bidId, :itemId, :bidderId, CAST(:bidAmount AS NUMERIC))";
        Map<String, Object> params = Map.of(
                "bidId", bidId,
                "itemId", createBidDto.getItemId(),
                "bidderId", createBidDto.getBidderId(),
                "bidAmount", createBidDto.getBidAmount()
        );

        try {
            namedParameterJdbcTemplate.update(sql, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to place bid: " + e.getMessage(), e);
        }
    }

    public void buyOut(String itemId, String bidderId) {
        String bidId = StringGenerator.generateRandomString(255);
        while (bidExists(bidId)) {
            bidId = StringGenerator.generateRandomString(255);
        }

        String sql = "CALL buy_out(:bidId, :itemId, :bidderId)";
        Map<String, Object> params = Map.of(
                "bidId", bidId,
                "itemId", itemId,
                "bidderId", bidderId
        );

        try {
            namedParameterJdbcTemplate.update(sql, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to buy out: " + e.getMessage(), e);
        }
    }

    public void finalizeAuctions() {
        String sql = "CALL finalize_auctions()";
        try {
            namedParameterJdbcTemplate.update(sql, Map.of());
        } catch (Exception e) {
            throw new RuntimeException("Failed to finalize auctions: " + e.getMessage(), e);
        }
    }

    public boolean bidExists(String bidId) {
        String sql = "SELECT COUNT(*) FROM bid WHERE bid_id = :bidId";
        Map<String, Object> params = Map.of("bidId", bidId);
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}
