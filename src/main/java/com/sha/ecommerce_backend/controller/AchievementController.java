package com.sha.ecommerce_backend.controller;

import com.sha.ecommerce_backend.dto.CreateAchievementDto;
import com.sha.ecommerce_backend.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {
    @Autowired private AchievementService achievementService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllAchievements() {
        try {
            return ResponseEntity.ok(achievementService.getAllAchievements());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to retrieve achievements: " + e.getMessage());
        }
    }

    @GetMapping("/id/{achievementId}")
    public ResponseEntity<?> getAchievementById(@PathVariable String achievementId) {
        try {
            return ResponseEntity.ok(achievementService.getAchievementById(achievementId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to retrieve achievement with ID " + achievementId + ": " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAchievement(@RequestBody CreateAchievementDto createAchievementDto) {
        try {
            achievementService.saveAchievement(createAchievementDto);
            return ResponseEntity.status(201).body("Achievement created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to save achievement: " + e.getMessage());
        }
    }

    @PutMapping("/update/{achievementId}")
    public ResponseEntity<?> updateAchievement(@PathVariable String achievementId, @RequestBody CreateAchievementDto createAchievementDto) {
        try {
            achievementService.updateAchievement(achievementId, createAchievementDto);
            return ResponseEntity.ok("Achievement updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update achievement with ID " + achievementId + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{achievementId}")
    public ResponseEntity<?> deleteAchievement(@PathVariable String achievementId) {
        try {
            achievementService.deleteAchievement(achievementId);
            return ResponseEntity.ok("Achievement deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete achievement with ID " + achievementId + ": " + e.getMessage());
        }
    }
}
