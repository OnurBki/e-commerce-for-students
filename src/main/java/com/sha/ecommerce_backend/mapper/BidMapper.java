package com.sha.ecommerce_backend.mapper;

import com.sha.ecommerce_backend.dto.GetBidDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class BidMapper {
    public RowMapper<GetBidDto> getBidRowMapper() {
        return (rs, rowNum) -> {
            GetBidDto bid = new GetBidDto();
            bid.setBidId(rs.getString("bid_id"));
            bid.setBidAmount(rs.getFloat("bid_amount"));
            bid.setBidTime(rs.getTimestamp("bid_time").toString());
            bid.setItemId(rs.getString("item_id"));
            bid.setBidderId(rs.getString("user_id"));
            return bid;
        };
    }


}
