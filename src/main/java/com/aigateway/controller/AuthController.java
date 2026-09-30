package com.aigateway.controller;

import java.util.Map;

import com.aigateway.dto.AuthResponse;
import com.aigateway.dto.LoginRequest;
import com.aigateway.dto.RegisterRequest;
import com.aigateway.entity.User;
import com.aigateway.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and JWT login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a user")
    public ResponseEntity<?> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"username\":\"demo\",\"password\":\"password123\"}"
                            )
                    )
            )
            @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", user.getId(),
                "username", user.getUsername()
        ));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"username\":\"demo\",\"password\":\"password123\"}"
                            )
                    )
            )
            @RequestBody LoginRequest request
    ) {
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token, "Bearer"));
    }

}
