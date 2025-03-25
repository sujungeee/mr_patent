package com.d208.mr_patent_backend.domain.chat.controller;

import com.d208.mr_patent_backend.domain.chat.dto.ChatListDto;
import com.d208.mr_patent_backend.domain.chat.dto.ChatRoomCreateRequest;
import com.d208.mr_patent_backend.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;


    // 채팅방 목록 조회
    @GetMapping("/{userId}")
    public Map<String, Object> getChatRoomsByUserId(@PathVariable Integer userId) {
        List<ChatListDto> chatRooms = chatRoomService.getChatRoomsByUserId(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", chatRooms);
        return response;
    }
    @PostMapping("/create")
    public ResponseEntity<String> createChatRoom(@RequestBody ChatRoomCreateRequest request) {
        String roomId = chatRoomService.createChatRoom(request.getUserId(), request.getReceiverId());
        return ResponseEntity.ok(roomId);
    }
}
