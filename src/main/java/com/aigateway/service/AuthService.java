package com.aigateway.service;

import java.time.LocalDateTime;

import com.aigateway.dto.LoginRequest;
import com.aigateway.dto.RegisterRequest;
import com.aigateway.entity.User;
import com.aigateway.repository.UserRepository;
import com.aigateway.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());
        String username = request.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException();
        }

        User user = new User(
                username,
                passwordEncoder.encode(request.getPassword()),
                LocalDateTime.now()
        );
        return userRepository.save(user);
    }

    public String login(LoginRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());
        String username = request.getUsername().trim();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        return jwtService.generateToken(user);
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
    }

    public static class UsernameAlreadyExistsException extends RuntimeException {

        public UsernameAlreadyExistsException() {
            super("Username already exists.");
        }
    }
}
