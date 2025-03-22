package com.d208.mr_patent_backend.domain.chat.service;

import com.d208.mr_patent_backend.domain.chat.dto.ChatMessageDto;
import com.d208.mr_patent_backend.domain.chat.entity.ChatMessage;
import com.d208.mr_patent_backend.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {


    private final ChatMessageRepository chatMessageRepository;

//    public ChatService(ChatMessageRepository chatMessageRepository) {
//        this.chatMessageRepository = chatMessageRepository;
//    }

    public void saveMessage(ChatMessageDto dto) {
        ChatMessage message = ChatMessage.builder()
                .chatId(dto.getChatId())
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                .message(dto.getMessage())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .isRead(dto.getIsRead())
                .build();

        chatMessageRepository.save(message);
    }
}
