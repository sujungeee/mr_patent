package com.d208.mr_patent_backend.domain.chat.service;

import com.d208.mr_patent_backend.domain.chat.dto.ChatListDto;
import com.d208.mr_patent_backend.domain.chat.entity.ChatRoom;
import com.d208.mr_patent_backend.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public List<ChatListDto> getChatRoomsByUserId(Integer userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserId(userId);

    // userId에 따른 채팅방 목록 조회
        return chatRooms.stream()
                .map(room -> ChatListDto.builder()
                        .roomId(room.getRoomId())
                        .lastMessage(room.getLastMessage())
                        .lastTimestamp(room.getLastTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}