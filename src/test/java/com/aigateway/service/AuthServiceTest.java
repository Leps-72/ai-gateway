package com.aigateway.service;

import com.aigateway.dto.LoginRequest;
import com.aigateway.dto.RegisterRequest;
import com.aigateway.entity.User;
import com.aigateway.repository.UserRepository;
import com.aigateway.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void registerStoresBcryptHashInsteadOfPlaintextPassword() {
        UserRepository repository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        when(repository.existsByUsername("demo")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuthService service = new AuthService(repository, encoder, jwtService);
        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        request.setPassword("password123");

        User saved = service.register(request);

        assertNotEquals("password123", saved.getPasswordHash());
        assertTrue(saved.getPasswordHash().startsWith("$2"));
        assertTrue(encoder.matches("password123", saved.getPasswordHash()));
        verify(repository).save(saved);
    }

    @Test
    void loginVerifiesBcryptHashAndReturnsCustomJwt() {
        UserRepository repository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User(
                "demo",
                encoder.encode("password123"),
                LocalDateTime.now()
        );
        user.setId(7L);
        when(repository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("custom-jwt");
        AuthService service = new AuthService(repository, encoder, jwtService);
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("password123");

        String token = service.login(request);

        assertEquals("custom-jwt", token);
        verify(jwtService).generateToken(user);
    }
}
