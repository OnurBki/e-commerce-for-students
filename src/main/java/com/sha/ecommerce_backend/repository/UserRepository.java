package com.sha.ecommerce_backend.repository;

import com.sha.ecommerce_backend.dto.CreateUserDto;
import com.sha.ecommerce_backend.dto.GetUserDto;
import com.sha.ecommerce_backend.mapper.UserRowMapper;
import com.sha.ecommerce_backend.model.User;
import com.sha.ecommerce_backend.util.StringGenerator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserRowMapper userRowMapper;
    private final PasswordEncoder passwordEncoder;

    public UserRepository(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            UserRowMapper userRowMapper, PasswordEncoder passwordEncoder)
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.userRowMapper = userRowMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<GetUserDto> findAll(int offset, int limit) {
        String sql = "SELECT * FROM \"user\" ORDER BY user_name LIMIT :limit OFFSET :offset";
        Map<String, Object> params = Map.of("limit", limit, "offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, userRowMapper.getUserDtoRowMapper());
    }

    public User findByUsernameUserDetails(String userName) {

        String sql = "SELECT * FROM \"user\" WHERE user_name = :userName";
        Map<String, Object> params = Map.of("userName", userName);

        List<User> users = namedParameterJdbcTemplate.query(sql, params, userRowMapper.userRowMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    public GetUserDto findByUsername(String userName) {
        String sql = "SELECT * FROM \"user\" WHERE user_name = :userName";
        Map<String, Object> params = Map.of("userName", userName);

        List<GetUserDto> users = namedParameterJdbcTemplate.query(sql, params, userRowMapper.getUserDtoRowMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    public GetUserDto findById(String id) {
        String sql = "SELECT * FROM \"user\" WHERE user_id = :id";
        Map<String, Object> params = Map.of("id", id);

        List<GetUserDto> users = namedParameterJdbcTemplate.query(sql, params, userRowMapper.getUserDtoRowMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    @Transactional
    public String save(CreateUserDto userDto) {
        String userId = StringGenerator.generateRandomString(255);
        // Ensure the userId is unique
        while (findById(userId) != null) {
            userId = StringGenerator.generateRandomString(255);
        }

        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());

        User user = new User();
        user.setId(userId);
        user.setUserName(userDto.getUserName());
        user.setHashedPassword(hashedPassword);
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setStudentId(userDto.getStudentId());

        String sql = "INSERT INTO \"user\" (user_id, user_name, hashed_password, email, phone_number, student_id, reputation, balance, is_admin, address) " +
                "VALUES (:userId, :userName, :hashedPassword, :email, :phoneNumber, :studentId, :reputation, :balance, :isAdmin, :address)";
        Map<String, Object> params = userRowMapper.createUserMapper(user);

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            if (rowsAffected != 1) {
                throw new RuntimeException("Failed to create user: no rows affected");
            }
            return userId;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to create user: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM \"user\"";
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }
}
