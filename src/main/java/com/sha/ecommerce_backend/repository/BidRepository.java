package com.sha.ecommerce_backend.repository;

import com.sha.ecommerce_backend.dto.CreateBidDto;
import com.sha.ecommerce_backend.dto.GetBidDto;
import com.sha.ecommerce_backend.dto.GetUserDto;
import com.sha.ecommerce_backend.dto.GetItemDto;
import com.sha.ecommerce_backend.mapper.BidMapper;
import com.sha.ecommerce_backend.util.StringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class BidRepository {
    @Autowired private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired private BidMapper bidMapper;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    public List<GetBidDto> getBidsByItemId(String itemId) {
        String sql = "SELECT * FROM bid WHERE item_id = :itemId ORDER BY bid_time DESC";
        Map<String, Object> params = Map.of("itemId", itemId);
        return namedParameterJdbcTemplate.query(sql, params, bidMapper.getBidRowMapper());
    }

    public List<GetBidDto> getBidsByBidderId(String bidderId) {
        String sql = "SELECT * FROM bid WHERE user_id = :bidderId ORDER BY bid_time DESC";
        Map<String, Object> params = Map.of("bidderId", bidderId);
        return namedParameterJdbcTemplate.query(sql, params, bidMapper.getBidRowMapper());
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

            // Update item's cached current price
            String cacheItemKey = "item:" + createBidDto.getItemId();
            GetItemDto cachedItem = (GetItemDto) redisTemplate.opsForValue().get(cacheItemKey);

            // Update bidder's cached balance
            String userCacheKey = "user:id:" + createBidDto.getBidderId();
            GetUserDto cachedUser = (GetUserDto) redisTemplate.opsForValue().get(userCacheKey);

            if (cachedUser != null) {
                // Deduct bid amount from cached user's balance
                cachedUser.setBalance(cachedUser.getBalance() - createBidDto.getBidAmount());
                redisTemplate.opsForValue().set(userCacheKey, cachedUser, 20, TimeUnit.MINUTES);
            }
            if (cachedItem != null) {
                // Update cached item's current price
                cachedItem.setCurrentPrice(createBidDto.getBidAmount());
                redisTemplate.opsForValue().set(cacheItemKey, cachedItem, 20, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to place bid: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void buyOut(String itemId, String bidderId) {
        String bidId = StringGenerator.generateRandomString(255);
        while (bidExists(bidId)) {
            bidId = StringGenerator.generateRandomString(255);
        }

        // Get all bidders who participated in this auction (for cache updates)
        String biddersSql = "SELECT DISTINCT user_id FROM bid WHERE item_id = :itemId";
        Map<String, Object> biddersParams = Map.of("itemId", itemId);
        List<String> affectedBidderIds = namedParameterJdbcTemplate.queryForList(biddersSql, biddersParams, String.class);

        // Get seller ID for cache invalidation
        String sellerSql = "SELECT user_id FROM item WHERE item_id = :itemId";
        Map<String, Object> sellerParams = Map.of("itemId", itemId);
        String sellerId = namedParameterJdbcTemplate.queryForObject(sellerSql, sellerParams, String.class);

        String sql = "CALL buy_out(:bidId, :itemId, :bidderId)";
        Map<String, Object> params = Map.of(
                "bidId", bidId,
                "itemId", itemId,
                "bidderId", bidderId
        );

        try {
            namedParameterJdbcTemplate.update(sql, params);

            // Invalidate all affected user caches
            for (String userId : affectedBidderIds) {
                redisTemplate.delete("user:id:" + userId);
            }

            // Invalidate seller's cache
            if (sellerId != null) {
                redisTemplate.delete("user:id:" + sellerId);
            }

            // Invalidate the item cache
            redisTemplate.delete("item:" + itemId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to buy out: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void finalizeAuctions() {
        try {
            // Get all bidders from auctions that will be finalized
            String biddersSql = "SELECT DISTINCT b.user_id FROM bid b " +
                    "JOIN item i ON b.item_id = i.item_id " +
                    "WHERE i.auction_end_time <= NOW() AND i.status = 0";
            List<String> userIds = namedParameterJdbcTemplate.queryForList(biddersSql, Map.of(), String.class);

            // Also get sellers of these items
            String sellersSql = "SELECT DISTINCT user_id FROM item " +
                    "WHERE auction_end_time <= NOW() AND status = 0";
            List<String> sellerIds = namedParameterJdbcTemplate.queryForList(sellersSql, Map.of(), String.class);

            // Get all cached items that will be finalized
            String itemsSql = "SELECT item_id FROM item WHERE auction_end_time <= NOW() AND status = 0";
            List<String> itemIds = namedParameterJdbcTemplate.queryForList(itemsSql, Map.of(), String.class);

            // Combine both lists
            userIds.addAll(sellerIds);

            // Call the stored procedure to finalize auctions
            String sql = "CALL finalize_auctions()";
            namedParameterJdbcTemplate.update(sql, Map.of());

            // Update or delete cache for affected users
            for (String userId : userIds) {
                String cacheKey = "user:id:" + userId;
                redisTemplate.delete(cacheKey);
            }
            for (String itemId : itemIds) {
                redisTemplate.delete("item:" + itemId);
            }
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

    public List<Map<String, Object>> getAllBidsForReports() {
        String sql = """
            SELECT
                bid_id, bid_amount, bid_time, item_id, user_id
            FROM bid
            ORDER BY bid_time DESC
        """;
        
        return namedParameterJdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> bid = new HashMap<>();
            bid.put("bidId", rs.getString("bid_id"));
            bid.put("bidAmount", rs.getFloat("bid_amount"));
            bid.put("bidTime", rs.getTimestamp("bid_time"));
            bid.put("itemId", rs.getString("item_id"));
            bid.put("bidderId", rs.getString("user_id"));
            return bid;
        });
    }
}
