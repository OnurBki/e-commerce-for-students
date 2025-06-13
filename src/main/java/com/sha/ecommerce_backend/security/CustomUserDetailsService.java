package com.sha.ecommerce_backend.security;

import com.sha.ecommerce_backend.model.User;
import com.sha.ecommerce_backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameUserDetails(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Convert isAdmin field to roles
        String role = user.getIsAdmin() ? "ROLE_ADMIN" : "ROLE_USER";

        // Return a user object with roles (authorities)
        return new org.springframework.security.core.userdetails.User(
                user.getUserName(),
                user.getHashedPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role)) // Map boolean to ROLE
        );
    }
}
