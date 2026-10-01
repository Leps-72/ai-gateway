package com.aigateway.repository;

import com.aigateway.entity.Conversation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:conversation-repository-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository repository;

    @Test
    void persistsMessageLongerThanDefaultVarcharLength() {
        String longMessage = "x".repeat(1_000);
        Conversation conversation = conversation(1L, longMessage);

        Conversation saved = repository.saveAndFlush(conversation);
        Conversation reloaded = repository.findById(saved.getId()).orElseThrow();

        assertEquals(longMessage, reloaded.getMessage());
    }

    @Test
    void findsOnlyConversationsOwnedByRequestedUser() {
        repository.saveAllAndFlush(List.of(
                conversation(1L, "A1"),
                conversation(1L, "A2"),
                conversation(2L, "B1")
        ));

        List<Conversation> userAConversations = repository.findByUserId(1L);

        assertEquals(List.of("A1", "A2"), userAConversations.stream()
                .map(Conversation::getMessage)
                .toList());
    }

    private Conversation conversation(Long userId, String message) {
        return new Conversation(
                message,
                "response",
                "gemini",
                "test-model",
                10L,
                5L,
                100L,
                userId,
                "success",
                LocalDateTime.now()
        );
    }
}
