package com.sha.ecommerce_backend.mapper;

import com.sha.ecommerce_backend.dto.CreateUserDto;
import com.sha.ecommerce_backend.dto.GetUserDto;
import com.sha.ecommerce_backend.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserMapper {
    public RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getString("user_id"));
            user.setUserName(rs.getString("user_name"));
            user.setHashedPassword(rs.getString("hashed_password"));
            user.setEmail(rs.getString("email"));
            user.setPhoneNumber(rs.getString("phone_number"));
            user.setAddress(rs.getString("address"));
            user.setStudentId(rs.getString("student_id"));
            user.setReputation(rs.getFloat("reputation"));
            user.setBalance(rs.getFloat("balance"));
            user.setIsAdmin(rs.getBoolean("is_admin"));
            return user;
        };
    }

    public RowMapper<GetUserDto> getUserDtoRowMapper() {
        return (rs, rowNum) -> {
            GetUserDto user = new GetUserDto();
            user.setId(rs.getString("user_id"));
            user.setUserName(rs.getString("user_name"));
            user.setEmail(rs.getString("email"));
            user.setPhoneNumber(rs.getString("phone_number"));
            user.setAddress(rs.getString("address"));
            user.setStudentId(rs.getString("student_id"));
            user.setReputation(rs.getFloat("reputation"));
            user.setBalance(rs.getFloat("balance"));
            user.setAdmin(rs.getBoolean("is_admin"));
            return user;
        };
    }

    public Map<String, Object> createUserMapper(User user) {
        return Map.of(
                "userId", user.getId(),
                "userName", user.getUserName(),
                "hashedPassword", user.getHashedPassword(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "studentId", user.getStudentId(),
                "reputation", user.getReputation(),
                "balance", user.getBalance(),
                "isAdmin", false,
                "address", user.getAddress()
        );
    }

    public User createDtoToUserMapper(CreateUserDto createUserDto, String userId, String hashedPassword) {
        User user = new User();
        user.setId(userId);
        user.setUserName(createUserDto.getUserName());
        user.setHashedPassword(hashedPassword);
        user.setEmail(createUserDto.getEmail());
        user.setPhoneNumber(createUserDto.getPhoneNumber());
        user.setAddress(createUserDto.getAddress());
        user.setStudentId(createUserDto.getStudentId());
        return user;
    }

    public GetUserDto userToGetUserDtoMapper(User user) {
        GetUserDto getUserDto = new GetUserDto();
        getUserDto.setId(user.getId());
        getUserDto.setUserName(user.getUserName());
        getUserDto.setEmail(user.getEmail());
        getUserDto.setPhoneNumber(user.getPhoneNumber());
        getUserDto.setAddress(user.getAddress());
        getUserDto.setStudentId(user.getStudentId());
        getUserDto.setReputation(user.getReputation());
        getUserDto.setBalance(user.getBalance());
        getUserDto.setAdmin(user.getIsAdmin());
        return getUserDto;
    }
}
