package com.sha.ecommerce_backend.controller;

import com.sha.ecommerce_backend.dto.CreateUserDto;
import com.sha.ecommerce_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.KeyException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            return ResponseEntity.ok(userService.getAllUsers(page, size));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving users: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/username/{userName}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String userName) {
        try {
            return ResponseEntity.ok(userService.getUserByUsername(userName));
        } catch (KeyException e) {
            return ResponseEntity.status(404).body("User not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (KeyException e) {
            return ResponseEntity.status(404).body("User not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody CreateUserDto userDto) {
        try {
            String token = userService.createUser(userDto);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
    }
}
