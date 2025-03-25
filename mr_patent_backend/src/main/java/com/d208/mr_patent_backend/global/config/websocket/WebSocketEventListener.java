package com.d208.mr_patent_backend.global.config.websocket;
import com.d208.mr_patent_backend.domain.chat.dto.ChatRoomDto;
import com.d208.mr_patent_backend.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {


    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        System.out.println("✅ [SUBSCRIBE 이벤트 발생]");

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        String userId = accessor.getFirstNativeHeader("userId");
        String roomId = accessor.getFirstNativeHeader("roomId");

        System.out.println("🧾 [SUBSCRIBE] sessionId = " + sessionId + ", userId = " + userId + ", roomId = " + roomId);

        if (sessionId != null && userId != null && roomId != null) {
            int uid = Integer.parseInt(userId);

            chatRoomRepository.findByRoomIdAndUserId(roomId, uid).ifPresent(room -> {
                // 1. sessionId 저장
                room.setSessionId(sessionId);

                // 2. status 증가
                room.setStatus(room.getStatus() + 1);

                chatRoomRepository.save(room);

                // 3. 상태 전송
                messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                        ChatRoomDto.builder()
                                .roomId(roomId)
                                .status(room.getStatus())
                                .build());

                log.info("[입장] 유저 {} 채팅방 {} 입장, status: {}", uid, roomId, room.getStatus());
            });
        }
    }
}

