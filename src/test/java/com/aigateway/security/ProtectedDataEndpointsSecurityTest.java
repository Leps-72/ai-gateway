package com.aigateway.security;

import com.aigateway.controller.AuthController;
import com.aigateway.controller.ConversationController;
import com.aigateway.controller.HealthController;
import com.aigateway.controller.UsageController;
import com.aigateway.dto.UsageResponse;
import com.aigateway.entity.User;
import com.aigateway.service.AuthService;
import com.aigateway.service.ConversationService;
import com.aigateway.service.RateLimitService;
import com.aigateway.service.UsageService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        ConversationController.class,
        HealthController.class,
        UsageController.class
}, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ProtectedDataEndpointsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private UsageService usageService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @Test
    void publicEndpointsDoNotRequireAuthentication() throws Exception {
        User registeredUser = new User("demo", "bcrypt-hash", LocalDateTime.now());
        registeredUser.setId(1L);
        when(authService.register(any())).thenReturn(registeredUser);
        when(authService.login(any())).thenReturn("jwt-token");

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("demo"));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void validJwtAllowsProtectedEndpoint() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("demo");
        when(claims.get("userId")).thenReturn(7L);
        when(jwtService.parseToken("valid-token")).thenReturn(claims);
        when(usageService.getUsage(7L))
                .thenReturn(new UsageResponse(0L, 0L, 0.0, 0.0, null, true));

        mockMvc.perform(get("/usage")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requests").value(0));
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        when(jwtService.parseToken("invalid-token"))
                .thenThrow(new JwtException("invalid token"));

        mockMvc.perform(get("/usage")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_ERROR"));
    }

    @Test
    void defaultInMemoryUserDetailsServiceIsNotCreated() {
        assertTrue(applicationContext.getBeansOfType(UserDetailsService.class).isEmpty());
    }

    @Test
    void conversationsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/conversations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_ERROR"));
    }

    @Test
    void usageRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/usage"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_ERROR"));
    }
}
