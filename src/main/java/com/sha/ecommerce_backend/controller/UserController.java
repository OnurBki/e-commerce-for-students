package com.sha.ecommerce_backend.controller;

import com.sha.ecommerce_backend.dto.CreateUserDto;
import com.sha.ecommerce_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.KeyException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
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

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody CreateUserDto userDto,
                                        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            return ResponseEntity.ok(userService.updateUser(token, userDto));
        } catch (KeyException e) {
            return ResponseEntity.status(404).body("User not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating user: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/add-balance/{amount}")
    public ResponseEntity<?> addBalance(@PathVariable float amount,
                                        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            userService.addBalance(token, amount);
            return ResponseEntity.ok("Balance added successfully");
        } catch (KeyException e) {
            return ResponseEntity.status(404).body("User not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding balance: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            return ResponseEntity.ok(userService.deleteUser(token));
        } catch (KeyException e) {
            return ResponseEntity.status(404).body("User not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting user: " + e.getMessage());
        }
    }
}
