package com.d208.mr_patent_backend.domain.chat.service;

import com.d208.mr_patent_backend.domain.chat.dto.ChatMessageDto;
import com.d208.mr_patent_backend.domain.chat.entity.ChatMessage;
import com.d208.mr_patent_backend.domain.chat.entity.ChatRoom;
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
    @Transactional //두개의 작업을 하나의 트랜잭션으로 묶어버림 ( 하나라도 실패시 롤백)
    // 메세지 저장 로직 (dto -> 엔티티)
    public void saveMessage(ChatMessageDto dto) {
        ChatMessage message = ChatMessage.builder()
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                .message(dto.getMessage())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .isRead(dto.isRead())
                .build();

        chatMessageRepository.save(message);
        System.out.println("메시지 DB 저장 완료: " + dto.getMessage());

        //채팅방 데이터는 채팅방 접속해서 메세지 보낼때, 생성
        chatRoomRepository.findById(dto.getRoomId())
                .ifPresentOrElse(chatRoom -> {
                    // 채팅방이 이미 존재하면 lastMessage, timestamp 업데이트
                    chatRoom.setLastMessage(dto.getMessage());
                    chatRoom.setLastTimestamp(message.getTimestamp());
                    chatRoomRepository.save(chatRoom);
                }, () -> {
                    // 채팅방이 없으면 새로 생성해서 저장
                    ChatRoom newRoom = ChatRoom.builder()
//                            .roomId(dto.getRoomId())
                            .userId(dto.getUserId()) // 필요에 따라 설정
                            .lastMessage(dto.getMessage())
                            .lastTimestamp(message.getTimestamp())
                            .unreadcount(0)
                            .status(0)
                            .created(LocalDateTime.now())
                            .updated(LocalDateTime.now())
                            .build();
                    chatRoomRepository.save(newRoom);
                });
        System.out.println("메시지 저장 + 채팅방 업데이트 완료 (순수 JPA)");
    }
}