package com.sha.ecommerce_backend.repository;

import com.sha.ecommerce_backend.dto.CreateItemDto;
import com.sha.ecommerce_backend.dto.GetItemDto;
import com.sha.ecommerce_backend.mapper.ItemMapper;
import com.sha.ecommerce_backend.util.StringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class ItemRepository {
    @Autowired private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired private ItemMapper itemMapper;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    public List<GetItemDto> findAllOrderByAuctionTime(int offset, int limit, boolean isAscending) {
        String sql = "SELECT * FROM item ORDER BY auction_start_time " + (isAscending ? "ASC" : "DESC") + " LIMIT :limit OFFSET :offset";

        Map<String, Object> params = Map.of("limit", limit, "offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, itemMapper.getItemDtoRowMapper());
    }

    public List<GetItemDto> findAllOrderByStartingPrice(int offset, int limit, boolean isAscending) {
        String sql = "SELECT * FROM item ORDER BY starting_price " + (isAscending ? "ASC" : "DESC") + " LIMIT :limit OFFSET :offset";
        Map<String, Object> params = Map.of("limit", limit, "offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, itemMapper.getItemDtoRowMapper());
    }

    public List<GetItemDto> findAllOrderByCurrentPrice(int offset, int limit, boolean isAscending) {
        String sql = "SELECT * FROM item ORDER BY current_price " + (isAscending ? "ASC" : "DESC") + " LIMIT :limit OFFSET :offset";
        Map<String, Object> params = Map.of("limit", limit, "offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, itemMapper.getItemDtoRowMapper());
    }

    public List<GetItemDto> findAllOrderByBuyOutPrice(int offset, int limit, boolean isAscending) {
        String sql = "SELECT * FROM item ORDER BY buyout_price " + (isAscending ? "ASC" : "DESC") + " LIMIT :limit OFFSET :offset";
        Map<String, Object> params = Map.of("limit", limit, "offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, itemMapper.getItemDtoRowMapper());
    }

    public List<GetItemDto> findAllByCategory(int category, int offset, int limit) {
        String sql = "SELECT * FROM item WHERE category = :category ORDER BY auction_start_time DESC LIMIT :limit OFFSET :offset";
        Map<String, Object> params = Map.of("category", category, "limit", limit, "offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, itemMapper.getItemDtoRowMapper());
    }

    public List<GetItemDto> findAllByStatus(int status, int offset, int limit) {
        String sql = "SELECT * FROM item WHERE status = :status ORDER BY auction_start_time DESC LIMIT :limit OFFSET :offset";
        Map<String, Object> params = Map.of("status", status, "limit", limit, "offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, itemMapper.getItemDtoRowMapper());
    }

    public GetItemDto findById(String itemId) {
        String cacheKey = "item:" + itemId;
        GetItemDto item = (GetItemDto) redisTemplate.opsForValue().get(cacheKey);
        if (item != null) {
            return item;
        }

        String sql = "SELECT * FROM item WHERE item_id = :itemId";
        Map<String, Object> params = Map.of("itemId", itemId);
        List<GetItemDto> items = namedParameterJdbcTemplate.query(sql, params, itemMapper.getItemDtoRowMapper());

        if (items.isEmpty()) {
            return null;
        }

        item = items.get(0);
        redisTemplate.opsForValue().set(cacheKey, item, 20, TimeUnit.MINUTES);

        return item;
    }

    @Transactional
    public String save(CreateItemDto createItemDto) {
        String itemId = StringGenerator.generateRandomString(255);
        while (existsById(itemId)) {
            itemId = StringGenerator.generateRandomString(255);
        }

        String sql = """
            INSERT INTO item (
                item_id, title, description, category, starting_price, current_price,
                buyout_price, condition, status, auction_start_time, auction_end_time, user_id, owner_id
            )
            SELECT
                :itemId, :title, :description, :category, :startingPrice, :currentPrice,
                :buyoutPrice, :condition, 0, :auctionStartTime, :auctionEndTime, :sellerId, :ownerId
            WHERE (
                SELECT COUNT(*) FROM item WHERE user_id = :sellerId AND status = 0
            ) < 10
            RETURNING item_id;
       """;
        Map<String, Object> params = itemMapper.createItemMapper(itemId, createItemDto);

        try {
            String insertedItemId = namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return rs.getString("item_id");
                } else {
                    return null; // INSERT didn't happen because cnt >= 10
                }
            });

            if (insertedItemId != null) {
                redisTemplate.opsForValue().set("item:" + insertedItemId, createItemDto, 20, TimeUnit.MINUTES);
                return insertedItemId;
            } else {
                throw new RuntimeException("Item creation failed: user already has 10 active items.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving item: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean updateCurrentPrice(String itemId, Float newCurrentPrice, String userId) {
        String cacheKey = "item:" + itemId;
        GetItemDto item = (GetItemDto) redisTemplate.opsForValue().get(cacheKey);
        if (item == null) {
            item = findById(itemId);
            if (item == null) {
                return false; // Item does not exist
            }
        }

        if (!item.getSellerId().equals(userId)) {
            return false; // User is not the owner of the item
        }

        item.setCurrentPrice(newCurrentPrice);
        String sql = "UPDATE item SET current_price = :currentPrice WHERE item_id = :itemId";
        Map<String, Object> params = Map.of("currentPrice", newCurrentPrice, "itemId", itemId);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        if (rowsAffected > 0) {
            redisTemplate.opsForValue().set(cacheKey, item, 20, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateStatus(String itemId, String newStatus, String userId) {
        String cacheKey = "item:" + itemId;
        GetItemDto item = (GetItemDto) redisTemplate.opsForValue().get(cacheKey);
        if (item == null) {
            item = findById(itemId);
            if (item == null) {
                return false; // Item does not exist
            }
        }

        if (!item.getSellerId().equals(userId)) {
            return false; // User is not the owner of the item
        }

        item.setStatus(newStatus);
        String sql = "UPDATE item SET status = :status WHERE item_id = :itemId";
        Map<String, Object> params = Map.of("status", newStatus, "itemId", itemId);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        if (rowsAffected > 0) {
            redisTemplate.opsForValue().set(cacheKey, item, 20, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean delete(String itemId) {
        String cacheKey = "item:" + itemId;
        redisTemplate.delete(cacheKey);

        String sql = "DELETE FROM item WHERE item_id = :itemId";
        Map<String, Object> params = Map.of("itemId", itemId);
        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected > 0;
    }

    public boolean existsById(String itemId) {
        String cacheKey = "item:" + itemId;
        Boolean hasKey = redisTemplate.hasKey(cacheKey);
        if (hasKey != null && hasKey) {
            return true;
        }

        String sql = "SELECT COUNT(*) FROM item WHERE item_id = :itemId";
        Map<String, Object> params = Map.of("itemId", itemId);
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}
