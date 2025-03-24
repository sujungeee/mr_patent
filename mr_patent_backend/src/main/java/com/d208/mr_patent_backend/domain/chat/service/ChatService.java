package com.d208.mr_patent_backend.domain.chat.service;

import com.d208.mr_patent_backend.domain.chat.dto.ChatMessageDto;
import com.d208.mr_patent_backend.domain.chat.entity.ChatMessage;
import com.d208.mr_patent_backend.domain.chat.repository.ChatMessageRepository;
import com.d208.mr_patent_backend.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }
    @Transactional
    public void saveMessage(ChatMessageDto dto) {
        ChatMessage message = ChatMessage.builder()
                .userId(dto.getUserId()) // 보낸 사람
                .message(dto.getMessage())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .isRead(false) // 처음 저장 시 읽음 여부는 false
                .build();
        chatMessageRepository.save(message);
        System.out.println("✅ 메시지 DB 저장 완료: " + dto.getMessage());
    }

}