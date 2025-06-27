package com.sha.ecommerce_backend.repository;

import com.sha.ecommerce_backend.dto.CreateAchievementDto;
import com.sha.ecommerce_backend.dto.GetAchievementDto;
import com.sha.ecommerce_backend.mapper.AchievementMapper;
import com.sha.ecommerce_backend.util.StringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AchievementRepository {
    @Autowired private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired private AchievementMapper achievementMapper;

    public List<GetAchievementDto> getAllAchievements() {
        String sql = "SELECT * FROM achievement";
        return namedParameterJdbcTemplate.query(sql, Map.of(), achievementMapper.getAchievementRowMapper());
    }

    public List<GetAchievementDto> getAchievementsByUserId(String userId) {
        String sql = "SELECT a.* FROM achievement a " +
                "JOIN user_achievement ua ON a.achievement_id = ua.achievement_id " +
                "WHERE ua.user_id = :userId";
        Map<String, Object> params = Map.of("userId", userId);
        return namedParameterJdbcTemplate.query(sql, params, achievementMapper.getAchievementRowMapper());
    }

    public GetAchievementDto getAchievementById(String achievementId) {
        String sql = "SELECT * FROM achievement WHERE achievement_id = :achievementId";
        Map<String, Object> params = Map.of("achievementId", achievementId);
        List<GetAchievementDto> achievements = namedParameterJdbcTemplate.query(sql, params, achievementMapper.getAchievementRowMapper());
        if (achievements.isEmpty()) {
            throw new IllegalArgumentException("Achievement with ID " + achievementId + " does not exist.");
        }
        return achievements.get(0);
    }

    public void saveAchievement(CreateAchievementDto createAchievementDto) {
        String achievementId = StringGenerator.generateRandomString(255);
        while (achievementExists(achievementId)) {
            achievementId = StringGenerator.generateRandomString(255);
        }

        String sql = "INSERT INTO achievement (achievement_id, title, description, award_amount) VALUES (:title, :description, :awardAmount)";
        Map<String, Object> params = achievementMapper.createAchievementParams(createAchievementDto, achievementId);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public void updateAchievement(String achievementId, CreateAchievementDto createAchievementDto) {
        if (!achievementExists(achievementId)) {
            throw new IllegalArgumentException("Achievement with ID " + achievementId + " does not exist.");
        }

        String sql = "UPDATE achievement SET title = :title, description = :description, award_amount = :awardAmount WHERE achievement_id = :achievementId";
        Map<String, Object> params = achievementMapper.createAchievementParams(createAchievementDto, achievementId);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public void deleteAchievement(String achievementId) {
        if (!achievementExists(achievementId)) {
            throw new IllegalArgumentException("Achievement with ID " + achievementId + " does not exist.");
        }

        String sql = "DELETE FROM achievement WHERE achievement_id = :achievementId";
        Map<String, Object> params = Map.of("achievementId", achievementId);
        namedParameterJdbcTemplate.update(sql, params);
    }

    private boolean achievementExists(String achievementId) {
        String sql = "SELECT COUNT(*) FROM achievement WHERE achievement_id = :achievementId";
        Map<String, Object> params = Map.of("achievementId", achievementId);
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}
