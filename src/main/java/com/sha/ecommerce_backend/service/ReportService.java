package com.sha.ecommerce_backend.service;

import com.sha.ecommerce_backend.model.enums.ItemEnums;
import com.sha.ecommerce_backend.repository.ItemRepository;
import com.sha.ecommerce_backend.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    @Autowired private ItemRepository itemRepository;
    @Autowired private BidRepository bidRepository;

    public Map<String, Object> getSuccessRateByCategory() {
        try {
            // Get all items grouped by category and status
            List<Map<String, Object>> items = itemRepository.getAllItemsForReports();
            
            Map<String, Map<String, Integer>> categoryStats = new HashMap<>();
            
            // Initialize categories
            for (ItemEnums.ItemCategory category : ItemEnums.ItemCategory.values()) {
                categoryStats.put(category.name(), new HashMap<>());
                categoryStats.get(category.name()).put("SOLD", 0);
                categoryStats.get(category.name()).put("EXPIRED", 0);
                categoryStats.get(category.name()).put("ACTIVE", 0);
            }
            
            // Count items by category and status
            for (Map<String, Object> item : items) {
                String categoryStr = (String) item.get("category");
                String statusStr = (String) item.get("status");
                
                // Convert string values to enum names
                String category = null;
                String status = null;
                
                try {
                    int categoryValue = Integer.parseInt(categoryStr);
                    int statusValue = Integer.parseInt(statusStr);
                    category = ItemEnums.ItemCategory.fromValue(categoryValue).name();
                    status = ItemEnums.ItemStatus.fromValue(statusValue).name();
                } catch (Exception e) {
                    // Skip items with invalid category or status values
                    continue;
                }
                
                if (categoryStats.containsKey(category)) {
                    Map<String, Integer> stats = categoryStats.get(category);
                    if (status.equals("SOLD")) {
                        stats.put("SOLD", stats.get("SOLD") + 1);
                    } else if (status.equals("EXPIRED")) {
                        stats.put("EXPIRED", stats.get("EXPIRED") + 1);
                    } else if (status.equals("ACTIVE")) {
                        stats.put("ACTIVE", stats.get("ACTIVE") + 1);
                    }
                }
            }
            
            // Calculate success rates
            List<Map<String, Object>> successRates = new ArrayList<>();
            for (Map.Entry<String, Map<String, Integer>> entry : categoryStats.entrySet()) {
                String category = entry.getKey();
                Map<String, Integer> stats = entry.getValue();
                
                int sold = stats.get("SOLD");
                int expired = stats.get("EXPIRED");
                int total = sold + expired;
                
                double successRate = total > 0 ? (double) sold / total * 100 : 0;
                
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("category", category);
                categoryData.put("sold", sold);
                categoryData.put("expired", expired);
                categoryData.put("active", stats.get("ACTIVE"));
                categoryData.put("successRate", Math.round(successRate * 100.0) / 100.0);
                
                successRates.add(categoryData);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("successRates", successRates);
            result.put("totalCategories", successRates.size());
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate success rate by category report: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getBiddingBehaviorAnalysis() {
        try {
            // Get all bids with user information
            List<Map<String, Object>> bids = bidRepository.getAllBidsForReports();
            
            Map<String, List<Map<String, Object>>> bidsByUser = new HashMap<>();
            
            // Group bids by user
            for (Map<String, Object> bid : bids) {
                String bidderId = (String) bid.get("bidderId");
                if (!bidsByUser.containsKey(bidderId)) {
                    bidsByUser.put(bidderId, new ArrayList<>());
                }
                bidsByUser.get(bidderId).add(bid);
            }
            
            // Calculate bidding statistics per user
            List<Map<String, Object>> userBiddingStats = new ArrayList<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : bidsByUser.entrySet()) {
                String bidderId = entry.getKey();
                List<Map<String, Object>> userBids = entry.getValue();
                
                // Calculate average bid amount
                double avgBidAmount = userBids.stream()
                    .mapToDouble(bid -> ((Number) bid.get("bidAmount")).doubleValue())
                    .average()
                    .orElse(0.0);
                
                // Count unique items bid on
                long uniqueItems = userBids.stream()
                    .map(bid -> bid.get("itemId"))
                    .distinct()
                    .count();
                
                Map<String, Object> userStats = new HashMap<>();
                userStats.put("bidderId", bidderId);
                userStats.put("totalBids", userBids.size());
                userStats.put("uniqueItems", uniqueItems);
                userStats.put("averageBidAmount", Math.round(avgBidAmount * 100.0) / 100.0);
                
                userBiddingStats.add(userStats);
            }
            
            // Sort by total bids (most active bidders first) and take top 10
            userBiddingStats.sort((a, b) -> Integer.compare(
                ((Number) b.get("totalBids")).intValue(), 
                ((Number) a.get("totalBids")).intValue()
            ));
            
            List<Map<String, Object>> topBidders = new ArrayList<>();
            for (Map<String, Object> stats : userBiddingStats) {
                if (topBidders.size() >= 10) break;
                Map<String, Object> topBidder = new HashMap<>(stats);
                topBidder.remove("bidderId"); // Remove any user reference
                topBidders.add(topBidder);
            }
            
            // Calculate overall statistics
            double avgBidsPerUser = userBiddingStats.stream()
                .mapToDouble(stats -> ((Number) stats.get("totalBids")).doubleValue())
                .average()
                .orElse(0.0);
            
            double avgUniqueItemsPerUser = userBiddingStats.stream()
                .mapToDouble(stats -> ((Number) stats.get("uniqueItems")).doubleValue())
                .average()
                .orElse(0.0);
            
            // Calculate bid distribution statistics
            Map<String, Object> bidDistribution = new HashMap<>();
            bidDistribution.put("1-5 bids", userBiddingStats.stream().filter(s -> ((Number) s.get("totalBids")).intValue() <= 5).count());
            bidDistribution.put("6-10 bids", userBiddingStats.stream().filter(s -> {
                int bidCount = ((Number) s.get("totalBids")).intValue();
                return bidCount > 5 && bidCount <= 10;
            }).count());
            bidDistribution.put("11-20 bids", userBiddingStats.stream().filter(s -> {
                int bidCount = ((Number) s.get("totalBids")).intValue();
                return bidCount > 10 && bidCount <= 20;
            }).count());
            bidDistribution.put("20+ bids", userBiddingStats.stream().filter(s -> ((Number) s.get("totalBids")).intValue() > 20).count());
            
            Map<String, Object> result = new HashMap<>();
            result.put("topBidders", topBidders);
            result.put("bidDistribution", bidDistribution);
            result.put("averageBidsPerUser", Math.round(avgBidsPerUser * 100.0) / 100.0);
            result.put("averageUniqueItemsPerUser", Math.round(avgUniqueItemsPerUser * 100.0) / 100.0);
            result.put("totalActiveBidders", userBiddingStats.size());
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate bidding behavior analysis: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getCategoryPerformance() {
        try {
            // Get all items with their final prices and status
            List<Map<String, Object>> items = itemRepository.getAllItemsForReports();
            
            Map<String, List<Map<String, Object>>> itemsByCategory = new HashMap<>();
            
            // Group items by category
            for (Map<String, Object> item : items) {
                String categoryStr = (String) item.get("category");
                String category = null;
                
                try {
                    int categoryValue = Integer.parseInt(categoryStr);
                    category = ItemEnums.ItemCategory.fromValue(categoryValue).name();
                } catch (Exception e) {
                    // Skip items with invalid category values
                    continue;
                }
                
                if (!itemsByCategory.containsKey(category)) {
                    itemsByCategory.put(category, new ArrayList<>());
                }
                itemsByCategory.get(category).add(item);
            }
            
            // Calculate performance metrics for each category
            List<Map<String, Object>> categoryPerformance = new ArrayList<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : itemsByCategory.entrySet()) {
                String category = entry.getKey();
                List<Map<String, Object>> categoryItems = entry.getValue();
                
                // Filter sold items for revenue calculation
                List<Map<String, Object>> soldItems = categoryItems.stream()
                    .filter(item -> {
                        String statusStr = (String) item.get("status");
                        try {
                            int statusValue = Integer.parseInt(statusStr);
                            return ItemEnums.ItemStatus.fromValue(statusValue) == ItemEnums.ItemStatus.SOLD;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
                
                // Calculate revenue (sum of final prices for sold items)
                double totalRevenue = soldItems.stream()
                    .mapToDouble(item -> ((Number) item.get("currentPrice")).doubleValue())
                    .sum();
                
                // Calculate average price increase for sold items
                double avgPriceIncrease = soldItems.stream()
                    .mapToDouble(item -> {
                        double startingPrice = ((Number) item.get("startingPrice")).doubleValue();
                        double finalPrice = ((Number) item.get("currentPrice")).doubleValue();
                        return startingPrice > 0 ? ((finalPrice - startingPrice) / startingPrice) * 100 : 0;
                    })
                    .average()
                    .orElse(0.0);
                
                // Calculate average starting price
                double avgStartingPrice = categoryItems.stream()
                    .mapToDouble(item -> ((Number) item.get("startingPrice")).doubleValue())
                    .average()
                    .orElse(0.0);
                
                Map<String, Object> performance = new HashMap<>();
                performance.put("category", category);
                performance.put("totalItems", categoryItems.size());
                performance.put("soldItems", soldItems.size());
                performance.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
                performance.put("averagePriceIncrease", Math.round(avgPriceIncrease * 100.0) / 100.0);
                performance.put("averageStartingPrice", Math.round(avgStartingPrice * 100.0) / 100.0);
                
                categoryPerformance.add(performance);
            }
            
            // Sort by total revenue (most profitable first)
            categoryPerformance.sort((a, b) -> 
                Double.compare(((Number) b.get("totalRevenue")).doubleValue(), 
                             ((Number) a.get("totalRevenue")).doubleValue()));
            
            Map<String, Object> result = new HashMap<>();
            result.put("categoryPerformance", categoryPerformance);
            result.put("totalCategories", categoryPerformance.size());
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate category performance report: " + e.getMessage(), e);
        }
    }
} 