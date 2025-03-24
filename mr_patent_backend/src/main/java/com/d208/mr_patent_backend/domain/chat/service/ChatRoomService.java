package com.d208.mr_patent_backend.domain.chat.service;

import com.d208.mr_patent_backend.domain.chat.entity.ChatRoom;
import com.d208.mr_patent_backend.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom createChatRoom(Integer userId) {
        ChatRoom chatRoom = ChatRoom.builder()
                .userId(userId)
                .status(0)
                .unreadcount("0")
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    // userId에 따른 채팅방 목록 조회
    public List<ChatRoom> getUserChatRooms(Integer userId) {
        return chatRoomRepository.findByUserId(userId);
    }

}