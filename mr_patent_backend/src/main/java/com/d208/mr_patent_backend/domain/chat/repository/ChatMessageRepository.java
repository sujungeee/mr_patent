package com.d208.mr_patent_backend.domain.chat.repository;

import com.d208.mr_patent_backend.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findByRoomIdOrderByTimestampAsc(Integer roomId); // 채팅방Id
}

