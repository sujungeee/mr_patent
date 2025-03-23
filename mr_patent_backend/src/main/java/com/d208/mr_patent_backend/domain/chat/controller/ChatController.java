package com.d208.mr_patent_backend.domain.chat.controller;

import com.d208.mr_patent_backend.domain.chat.dto.ChatMessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 클라이언트가 "/pub/chat/message"로 메시지를 보내면 이 메서드가 처리(브로드 캐스트)
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDto message) {
        System.out.println("💬 받은 메시지: " + message.getMessage()); // 로그 찍히는지 확인용
        // DB 저장도 여기서 해도 됨

        // 특정 구독자들에게 메시지 전송 (구독 주소: /sub/chat/room/{roomId})
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}
