package com.sha.ecommerce_backend.controller;

import com.sha.ecommerce_backend.dto.CreateItemDto;
import com.sha.ecommerce_backend.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/items/")
public class ItemController {
    @Autowired private ItemService itemService;

    @GetMapping("order-by-auction-time")
    public ResponseEntity<?> getItemsOrderedByAuctionTime(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "isAscending", defaultValue = "true") boolean isAscending
    ) {
        try {
            return ResponseEntity.ok(itemService.getAllItemsByOrder(offset, limit, 0, isAscending));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching items: " + e.getMessage());
        }
    }

    @GetMapping("order-by-starting-price")
    public ResponseEntity<?> getItemsOrderedByStartingPrice(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "isAscending", defaultValue = "true") boolean isAscending
    ) {
        try {
            return ResponseEntity.ok(itemService.getAllItemsByOrder(offset, limit, 1, isAscending));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching items: " + e.getMessage());
        }
    }

    @GetMapping("order-by-current-price")
    public ResponseEntity<?> getItemsOrderedByCurrentPrice(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "isAscending", defaultValue = "true") boolean isAscending
    ) {
        try {
            return ResponseEntity.ok(itemService.getAllItemsByOrder(offset, limit, 2, isAscending));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching items: " + e.getMessage());
        }
    }

    @GetMapping("order-by-buyout-price")
    public ResponseEntity<?> getItemsOrderedByBuyoutPrice(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "isAscending", defaultValue = "true") boolean isAscending
    ) {
        try {
            return ResponseEntity.ok(itemService.getAllItemsByOrder(offset, limit, 3, isAscending));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching items: " + e.getMessage());
        }
    }

    @GetMapping("by-category")
    public ResponseEntity<?> getItemsByCategory(
            @RequestParam(name = "category") int category,
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(itemService.getAllItemsByFeature(0, category, offset, limit));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching items by category: " + e.getMessage());
        }
    }

    @GetMapping("by-status")
    public ResponseEntity<?> getItemsByStatus(
            @RequestParam(name = "status") int status,
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(itemService.getAllItemsByFeature(1, status, offset, limit));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching items by status: " + e.getMessage());
        }
    }

    @GetMapping("id/{itemId}")
    public ResponseEntity<?> getItemById(@PathVariable String itemId) {
        try {
            return ResponseEntity.ok(itemService.getItemById(itemId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching item by ID: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("create")
    public ResponseEntity<?> createItem(@RequestBody CreateItemDto item, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            String itemId = itemService.createItem(item, token);
            return ResponseEntity.ok(itemId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating item: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("update-current-price/{itemId}")
    public ResponseEntity<?> updateItemCurrentPrice(
            @PathVariable String itemId,
            @RequestBody Float currentPrice,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            itemService.updateItemCurrentPrice(itemId, currentPrice, token);
            return ResponseEntity.ok("Item current price updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating item current price: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("update-status/{itemId}")
    public ResponseEntity<?> updateItemStatus(
            @PathVariable String itemId,
            @RequestBody String status,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            itemService.updateItemStatus(itemId, status, token);
            return ResponseEntity.ok("Item status updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating item status: " + e.getMessage());
        }
    }
}
