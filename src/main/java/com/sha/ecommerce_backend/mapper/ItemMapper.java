package com.sha.ecommerce_backend.mapper;

import com.sha.ecommerce_backend.dto.CreateItemDto;
import com.sha.ecommerce_backend.dto.GetItemDto;
import com.sha.ecommerce_backend.model.Item;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ItemMapper {
    public RowMapper<GetItemDto> getItemDtoRowMapper() {
        return (rs, rowNum) -> {
            GetItemDto item = new GetItemDto();
            item.setItemId(rs.getString("item_id"));
            item.setTitle(rs.getString("title"));
            item.setDescription(rs.getString("description"));
            item.setCategory(rs.getString("category"));
            item.setStartingPrice(rs.getFloat("starting_price"));
            item.setCurrentPrice(rs.getFloat("current_price"));
            item.setBuyoutPrice(rs.getFloat("buyout_price"));
            item.setCondition(rs.getString("condition"));
            item.setStatus(rs.getString("status"));
            item.setAuctionStartTime(rs.getString("auction_start_time"));
            item.setAuctionEndTime(rs.getString("auction_end_time"));
            item.setSellerId(rs.getString("user_id"));
            item.setOwnerId(rs.getString("owner_id"));
            return item;
        };
    }

    public Map<String, Object> createItemMapper(String itemId, CreateItemDto item) {
        Map<String, Object> params = new HashMap<>();
        params.put("itemId", itemId);
        params.put("title", item.getTitle());
        params.put("description", item.getDescription());
        params.put("category", item.getCategory());
        params.put("startingPrice", item.getStartingPrice());
        params.put("currentPrice", item.getCurrentPrice());
        params.put("buyoutPrice", item.getBuyoutPrice());
        params.put("condition", item.getCondition());
        params.put("status", item.getStatus());
        params.put("auctionStartTime", item.getAuctionStartTime());
        params.put("auctionEndTime", item.getAuctionEndTime());
        params.put("sellerId", item.getSellerId());
        params.put("ownerId", item.getSellerId());
        return params;
    }
}
