package com.sha.ecommerce_backend.service;

import com.sha.ecommerce_backend.dto.CreateUserDto;
import com.sha.ecommerce_backend.dto.GetUserDto;
import com.sha.ecommerce_backend.repository.UserRepository;
import com.sha.ecommerce_backend.security.JwtUtils;
import org.springframework.stereotype.Service;

import java.security.KeyException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public UserService(UserRepository userRepository,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public Map<String, Object> getAllUsers(int page, int size) {
        try {
            int offset = page * size;
            List<GetUserDto> users = userRepository.findAll(offset, size);
            int totalUsers = userRepository.count();

            Map<String, Object> response = new HashMap<>();
            response.put("data", users);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalUsers", totalUsers);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving users: " + e.getMessage());
        }
    }

    public GetUserDto getUserByUsername(String username) throws Exception {
        try {
            GetUserDto user = userRepository.findByUsername(username);
            if (user == null) {
                throw new KeyException("User not found");
            }
            return user;
        } catch (KeyException e) {
            throw new KeyException("Error retrieving user: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("An unexpected error occurred: " + e.getMessage());
        }
    }

    public GetUserDto getUserById(String id) throws Exception {
        try {
            GetUserDto user = userRepository.findById(id);
            if (user == null) {
                throw new KeyException("User not found");
            }
            return user;
        } catch (KeyException e) {
            throw new KeyException("Error retrieving user: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("An unexpected error occurred: " + e.getMessage());
        }
    }

    public String createUser(CreateUserDto userDto) {
        try {
            GetUserDto existingUser = userRepository.findByUsername(userDto.getUserName());
            if (existingUser != null) {
                throw new IllegalArgumentException("User with username " + userDto.getUserName() + " already exists.");
            }

            String userId = userRepository.save(userDto);

            // Generate claims for JWT token
            Map<String, Object> claims = Map.of(
                    "userId", userId,
                    "role", false
            );

            return jwtUtils.generateToken(userId, claims);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("User creation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("User creation failed: " + e.getMessage(), e);
        }
    }
}
