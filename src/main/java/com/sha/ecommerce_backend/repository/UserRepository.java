package com.sha.ecommerce_backend.repository;

import com.sha.ecommerce_backend.dto.CreateUserDto;
import com.sha.ecommerce_backend.dto.GetUserDto;
import com.sha.ecommerce_backend.mapper.UserMapper;
import com.sha.ecommerce_backend.model.User;
import com.sha.ecommerce_backend.util.StringGenerator;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class UserRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserRepository(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            UserMapper userMapper,
            RedisTemplate<String, Object> redisTemplate,
            @Lazy PasswordEncoder passwordEncoder)
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public List<GetUserDto> findAll(int offset, int limit) {
        String sql = "SELECT * FROM \"user\" ORDER BY user_name LIMIT :limit OFFSET :offset";
        Map<String, Object> params = Map.of("limit", limit, "offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, userMapper.getUserDtoRowMapper());
    }

    public User findByUsernameUserDetails(String userName) {
        String cacheKey = "user:username:" + userName;
        User user = (User) redisTemplate.opsForValue().get(cacheKey);
        if (user != null) {
            return user;
        }

        String sql = "SELECT * FROM \"user\" WHERE user_name = :userName";
        Map<String, Object> params = Map.of("userName", userName);

        List<User> users = namedParameterJdbcTemplate.query(sql, params, userMapper.userRowMapper());

        user = users.isEmpty() ? null : users.get(0);

        if (user != null) {
            redisTemplate.opsForValue().set(cacheKey, user, 20, TimeUnit.MINUTES);
        }

        return user;
    }

    public GetUserDto findByUsername(String userName) {
        String cacheKey = "user:username" + userName;
        GetUserDto user = (GetUserDto) redisTemplate.opsForValue().get(cacheKey);
        if (user != null) {
            return user;
        }

        String sql = "SELECT * FROM \"user\" WHERE user_name = :userName";
        Map<String, Object> params = Map.of("userName", userName);

        List<GetUserDto> users = namedParameterJdbcTemplate.query(sql, params, userMapper.getUserDtoRowMapper());

        user = users.isEmpty() ? null : users.get(0);

        if (user != null) {
            redisTemplate.opsForValue().set(cacheKey, user, 20, TimeUnit.MINUTES);
        }

        return user;
    }

    public GetUserDto findById(String id) {
        String cacheKey = "user:id:" + id;
        GetUserDto user = (GetUserDto) redisTemplate.opsForValue().get(cacheKey);
        if (user != null) {
            return user;
        }

        String sql = "SELECT * FROM \"user\" WHERE user_id = :id";
        Map<String, Object> params = Map.of("id", id);

        List<GetUserDto> users = namedParameterJdbcTemplate.query(sql, params, userMapper.getUserDtoRowMapper());

        user = users.isEmpty() ? null : users.get(0);

        if (user != null) {
            redisTemplate.opsForValue().set(cacheKey, user, 20, TimeUnit.MINUTES);
        }

        return user;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM \"user\" WHERE email = :email";
        Map<String, Object> params = Map.of("email", email);

        List<User> users = namedParameterJdbcTemplate.query(sql, params, userMapper.userRowMapper());
        User user = users.isEmpty() ? null : users.get(0);

        String cacheKey = "user:email:" + email;
        if (user != null) {
            GetUserDto userDto = userMapper.userToGetUserDtoMapper(user);
            redisTemplate.opsForValue().set(cacheKey, userDto, 20, TimeUnit.MINUTES);
        }

        return user;
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

        User user = userMapper.createDtoToUserMapper(userDto, userId, hashedPassword);

        String sql = "INSERT INTO \"user\" (user_id, user_name, hashed_password, email, phone_number, student_id, reputation, balance, is_admin, address) " +
                "VALUES (:userId, :userName, :hashedPassword, :email, :phoneNumber, :studentId, :reputation, :balance, :isAdmin, :address)";
        Map<String, Object> params = userMapper.createUserMapper(user);

        redisTemplate.opsForValue().set("user:email:" + user.getEmail(), user, 20, TimeUnit.MINUTES);

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

    public GetUserDto update(String userId, CreateUserDto userDto) throws KeyException {
        GetUserDto user = (GetUserDto) redisTemplate.opsForValue().get("user:id:" + userId);
        if (user == null) {
            user = findById(userId);
            if (user == null) {
                throw new KeyException("User not found with id: " + userId);
            }
        }

        String sql = "UPDATE \"user\" SET user_name = :userName, email = :email, phone_number = :phoneNumber, address = :address, student_id = :studentId WHERE user_id = :userId";
        Map<String, Object> params = Map.of(
                "userName", userDto.getUserName(),
                "email", userDto.getEmail(),
                "phoneNumber", userDto.getPhoneNumber(),
                "address", userDto.getAddress(),
                "studentId", userDto.getStudentId(),
                "userId", userId
        );

        redisTemplate.opsForValue().set("user:id:" + userId, user, 20, TimeUnit.MINUTES);
        redisTemplate.delete("user:email:" + user.getEmail());
        redisTemplate.delete("user:username:" + user.getUserName());

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        if (rowsAffected != 1) {
            throw new RuntimeException("Failed to update user: no rows affected");
        }

        return findById(userId);
    }

    public boolean delete(String userId) {
        GetUserDto user = (GetUserDto) redisTemplate.opsForValue().get("user:id:" + userId);
        if (user == null) {
            user = findById(userId);
            if (user == null) {
                return false; // User not found
            }
        }

        String sql = "DELETE FROM \"user\" WHERE user_id = :userId";
        Map<String, Object> params = Map.of("userId", userId);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);

        if (rowsAffected > 0) {
            redisTemplate.delete("user:id:" + userId);
            redisTemplate.delete("user:email:" + user.getEmail());
            redisTemplate.delete("user:username:" + user.getUserName());
        }

        return rowsAffected == 1;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM \"user\"";
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    public boolean existByUsername(String userName) {
        User user = (User) redisTemplate.opsForValue().get("user:username:" + userName);
        if (user != null) {
            return true;
        }

        String sql = "SELECT COUNT(*) FROM \"user\" WHERE user_name = :userName";
        Map<String, Object> params = Map.of("userName", userName);
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public boolean existByEmail(String email) {
        User user = (User) redisTemplate.opsForValue().get("user:email:" + email);
        if (user != null) {
            return true;
        }

        String sql = "SELECT COUNT(*) FROM \"user\" WHERE email = :email";
        Map<String, Object> params = Map.of("email", email);
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}
