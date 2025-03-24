package com.d208.mr_patent_backend.domain.chat.controller;
//api/chat/rooms/{userId}

import com.d208.mr_patent_backend.domain.chat.dto.ChatListDto;
import com.d208.mr_patent_backend.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;


    // 채팅방 목록 조회
    @GetMapping("/{userId}")
    public List<ChatListDto> getChatRoomsByUserId(@PathVariable Integer userId) {
        return chatRoomService.getChatRoomsByUserId(userId);
    }
}
