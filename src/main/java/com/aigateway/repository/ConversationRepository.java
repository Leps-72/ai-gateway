package com.aigateway.repository;

import java.util.List;

import com.aigateway.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserId(Long userId);
}
