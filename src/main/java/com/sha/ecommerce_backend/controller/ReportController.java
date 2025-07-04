package com.sha.ecommerce_backend.controller;

import com.sha.ecommerce_backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/reports/")
public class ReportController {
    @Autowired private ReportService reportService;

    @GetMapping("success-rate-by-category")
    public ResponseEntity<?> getSuccessRateByCategory() {
        try {
            return ResponseEntity.ok(reportService.getSuccessRateByCategory());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching success rate by category: " + e.getMessage());
        }
    }

    @GetMapping("bidding-behavior")
    public ResponseEntity<?> getBiddingBehaviorAnalysis() {
        try {
            return ResponseEntity.ok(reportService.getBiddingBehaviorAnalysis());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching bidding behavior analysis: " + e.getMessage());
        }
    }

    @GetMapping("category-performance")
    public ResponseEntity<?> getCategoryPerformance() {
        try {
            return ResponseEntity.ok(reportService.getCategoryPerformance());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching category performance: " + e.getMessage());
        }
    }
} 