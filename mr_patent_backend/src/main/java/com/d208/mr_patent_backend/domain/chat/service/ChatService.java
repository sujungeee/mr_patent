package com.d208.mr_patent_backend.domain.chat.service;

import com.d208.mr_patent_backend.domain.chat.dto.ChatMessageDto;
import com.d208.mr_patent_backend.domain.chat.entity.ChatMessage;
import com.d208.mr_patent_backend.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatService {


    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    // 메세지 저장 로직 (dto -> 엔티티)
    public void saveMessage(ChatMessageDto dto) {
        ChatMessage message = ChatMessage.builder()
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                .message(dto.getMessage())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .isRead(dto.getIsRead())
                .build();

        chatMessageRepository.save(message);
        System.out.println("✅ 메시지 DB 저장 완료: " + dto.getMessage());
    }
}