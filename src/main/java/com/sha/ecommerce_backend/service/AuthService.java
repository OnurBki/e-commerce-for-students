package com.sha.ecommerce_backend.service;

import com.sha.ecommerce_backend.dto.CreateUserDto;
import com.sha.ecommerce_backend.dto.LoginDto;
import com.sha.ecommerce_backend.mapper.UserMapper;
import com.sha.ecommerce_backend.model.User;
import com.sha.ecommerce_backend.repository.UserRepository;
import com.sha.ecommerce_backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       RedisTemplate<String, Object> redisTemplate,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
    }

    public String login(LoginDto loginDto) {
        User user = (User) redisTemplate.opsForValue().get(loginDto.getEmail());
        if (user == null) {
            // If user is not in Redis, fetch from database
            user = userRepository.findByEmail(loginDto.getEmail());
            if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getHashedPassword())) {
                throw new BadCredentialsException("Wrong email or password");
            }
            // Store user in Redis for future requests
            redisTemplate.opsForValue().set(loginDto.getEmail(), user, 20, TimeUnit.MINUTES);
        }

        Map<String, Object> claims = Map.of(
                "userName", user.getUserName(),
                "role", user.getIsAdmin()
        );

        return jwtUtils.generateToken(user.getId(), claims);
    }

    public String register(CreateUserDto createUserDto) {
        boolean userExists = userRepository.existByEmail(createUserDto.getEmail());
        if (userExists) {
            throw new BadCredentialsException("User with this email already exists");
        }
        userExists = userRepository.existByUsername(createUserDto.getUserName());
        if (userExists) {
            throw new BadCredentialsException("User with this username already exists");
        }

        String userId = userRepository.save(createUserDto);
        String hashedPassword = passwordEncoder.encode(createUserDto.getPassword());
        User user = userMapper.createDtoToUserMapper(createUserDto, userId, hashedPassword);

        // Store user in Redis for future requests
        redisTemplate.opsForValue().set(user.getEmail(), user, 20, TimeUnit.MINUTES);

        Map<String, Object> claims = Map.of(
                "userName", user.getUserName(),
                "role", user.getIsAdmin()
        );

        return jwtUtils.generateToken(user.getId(), claims);
    }
}
