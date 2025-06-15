package com.sha.ecommerce_backend.service;

import com.sha.ecommerce_backend.dto.CreateItemDto;
import com.sha.ecommerce_backend.dto.GetItemDto;
import com.sha.ecommerce_backend.repository.ItemRepository;
import com.sha.ecommerce_backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ItemService {
    @Autowired private ItemRepository itemRepository;
    @Autowired private JwtUtils jwtUtils;

    public List<GetItemDto> getAllItemsByOrder(int offset, int limit, int orderBy, boolean isAscending) {
        return switch (orderBy) {
            case 0 -> itemRepository.findAllOrderByAuctionTime(offset, limit, isAscending);
            case 1 -> itemRepository.findAllOrderByStartingPrice(offset, limit, isAscending);
            case 2 -> itemRepository.findAllOrderByCurrentPrice(offset, limit, isAscending);
            case 3 -> itemRepository.findAllOrderByBuyOutPrice(offset, limit, isAscending);
            default -> throw new IllegalArgumentException("Invalid order by field: " + orderBy);
        };
    }

    public List<GetItemDto> getAllItemsByFeature(int feature, int value, int offset, int limit) {
        return switch (feature) {
            case 0 -> itemRepository.findAllByCategory(value, offset, limit);
            case 1 -> itemRepository.findAllByStatus(value, offset, limit);
            default -> throw new IllegalArgumentException("Invalid feature: " + feature);
        };
    }

    public GetItemDto getItemById(String itemId) {
        GetItemDto item = itemRepository.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found with ID: " + itemId);
        }
        return item;
    }

    public String createItem(CreateItemDto item, String token) {
        try {
            String userId = jwtUtils.extractUserId(token);
            if (!Objects.equals(userId, item.getSellerId())) {
                throw new IllegalArgumentException("Seller ID does not match the user ID from the token.");
            }
            return itemRepository.save(item);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create item: " + e.getMessage(), e);
        }
    }

    public void updateItemCurrentPrice(String itemId, float currentPrice, String token) {
        try {
            String userId = jwtUtils.extractUserId(token);
            boolean result = itemRepository.updateCurrentPrice(itemId, currentPrice, userId);
            if (!result) {
                throw new IllegalArgumentException("Item not found or update failed for ID: " + itemId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update item current price: " + e.getMessage(), e);
        }
    }

    public void updateItemStatus(String itemId, String status, String token) {
        try {
            String userId = jwtUtils.extractUserId(token);
            boolean result = itemRepository.updateStatus(itemId, status, userId);
            if (!result) {
                throw new IllegalArgumentException("Item not found or update failed for ID: " + itemId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update item status: " + e.getMessage(), e);
        }
    }

    public void deleteItem(String itemId) {
        try {
            boolean result = itemRepository.delete(itemId);
            if (!result) {
                throw new IllegalArgumentException("Item not found or deletion failed for ID: " + itemId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete item: " + e.getMessage(), e);
        }
    }
}
