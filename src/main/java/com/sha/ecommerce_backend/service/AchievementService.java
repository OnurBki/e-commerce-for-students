package com.sha.ecommerce_backend.service;

import com.sha.ecommerce_backend.dto.CreateAchievementDto;
import com.sha.ecommerce_backend.dto.GetAchievementDto;
import com.sha.ecommerce_backend.repository.AchievementRepository;
import com.sha.ecommerce_backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AchievementService {
    @Autowired private AchievementRepository achievementRepository;
    @Autowired private JwtUtils jwtUtils;

    public List<GetAchievementDto> getAllAchievements() {
        try {
            return achievementRepository.getAllAchievements();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve achievements: " + e.getMessage(), e);
        }
    }

    public List<GetAchievementDto> getAchievementsByUserId(String token) {
        String userId = null;
        try {
            userId = jwtUtils.extractUserId(token);
            return achievementRepository.getAchievementsByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve achievements for user ID " + userId + ": " + e.getMessage(), e);
        }
    }

    public GetAchievementDto getAchievementById(String achievementId) {
        try {
            return achievementRepository.getAchievementById(achievementId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve achievement with ID " + achievementId + ": " + e.getMessage(), e);
        }
    }

    public void saveAchievement(CreateAchievementDto createAchievementDto) {
        try {
            achievementRepository.saveAchievement(createAchievementDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save achievement: " + e.getMessage(), e);
        }
    }

    public void updateAchievement(String achievementId, CreateAchievementDto createAchievementDto) {
        try {
            achievementRepository.updateAchievement(achievementId, createAchievementDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update achievement with ID " + achievementId + ": " + e.getMessage(), e);
        }
    }

    public void deleteAchievement(String achievementId) {
        try {
            achievementRepository.deleteAchievement(achievementId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete achievement with ID " + achievementId + ": " + e.getMessage(), e);
        }
    }
}
