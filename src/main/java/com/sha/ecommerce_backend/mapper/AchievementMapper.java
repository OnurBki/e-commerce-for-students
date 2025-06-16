package com.sha.ecommerce_backend.mapper;

import com.sha.ecommerce_backend.dto.CreateAchievementDto;
import com.sha.ecommerce_backend.dto.GetAchievementDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AchievementMapper {
    public RowMapper<GetAchievementDto> getAchievementRowMapper() {
        return (rs, rowNum) -> {
            GetAchievementDto achievement = new GetAchievementDto();
            achievement.setAchievementId(rs.getString("achievement_id"));
            achievement.setTitle(rs.getString("title"));
            achievement.setDescription(rs.getString("description"));
            achievement.setAwardAmount(rs.getFloat("award_amount"));
            return achievement;
        };
    }

    public Map<String, Object> createAchievementParams(CreateAchievementDto createAchievementDto, String achievementId) {
        return Map.of(
            "achievementId", achievementId,
            "title", createAchievementDto.getTitle(),
            "description", createAchievementDto.getDescription(),
            "awardAmount", createAchievementDto.getAwardAmount()
        );
    }
}
