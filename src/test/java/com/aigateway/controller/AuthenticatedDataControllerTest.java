package com.aigateway.controller;

import com.aigateway.dto.UsageResponse;
import com.aigateway.entity.Conversation;
import com.aigateway.security.AuthenticatedUser;
import com.aigateway.service.ConversationService;
import com.aigateway.service.UsageService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticatedDataControllerTest {

    @Test
    void conversationsUseAuthenticatedPrincipalUserId() {
        ConversationService service = mock(ConversationService.class);
        Conversation a1 = conversation(1L, "A1");
        Conversation a2 = conversation(1L, "A2");
        when(service.getConversationsByUserId(1L)).thenReturn(List.of(a1, a2));
        ConversationController controller = new ConversationController(service);

        List<Conversation> response = controller.getConversations(authentication(1L, "user-a"));

        assertEquals(List.of("A1", "A2"), response.stream()
                .map(Conversation::getMessage)
                .toList());
        verify(service).getConversationsByUserId(1L);
    }

    @Test
    void usageUsesAuthenticatedPrincipalUserId() {
        UsageService service = mock(UsageService.class);
        UsageResponse expected = new UsageResponse(2L, 30L, 100.0, 0.0, null, false);
        when(service.getUsage(2L)).thenReturn(expected);
        UsageController controller = new UsageController(service);

        UsageResponse response = controller.getUsage(authentication(2L, "user-b"));

        assertEquals(expected, response);
        verify(service).getUsage(2L);
    }

    private UsernamePasswordAuthenticationToken authentication(Long userId, String username) {
        return new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userId, username),
                null
        );
    }

    private Conversation conversation(Long userId, String message) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setMessage(message);
        return conversation;
    }
}
