package com.d208.mr_patent_backend.domain.chat.service;
import com.d208.mr_patent_backend.domain.chat.entity.ChatRoom;
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
        LocalDateTime now = dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now();

        ChatMessage message = ChatMessage.builder()
                .roomId(dto.getRoomId())
                .userId(dto.getUserId()) // 보낸 사람
                .message(dto.getMessage())
                .timestamp(now)
                .isRead(false) // 처음 저장 시 읽음 여부는 false
                .build();
        chatMessageRepository.save(message);
        System.out.println(" 메시지 DB 저장 완료: " + dto.getMessage());


        // 3. ChatRoom row 가져오기
        ChatRoom senderRoom = chatRoomRepository.findByRoomIdAndUserId(dto.getRoomId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("보낸 사람 채팅방이 없습니다."));

        ChatRoom receiverRoom = chatRoomRepository.findByRoomIdAndUserId(dto.getRoomId(), dto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("받는 사람 채팅방이 없습니다."));

        // 4. senderRoom 업데이트
        senderRoom.setLastMessage(dto.getMessage());
        senderRoom.setLastTimestamp(now);
        senderRoom.setUpdated(now);

        // 5. receiverRoom 업데이트
        receiverRoom.setLastMessage(dto.getMessage());
        receiverRoom.setLastTimestamp(now);
        receiverRoom.setUnreadCount(receiverRoom.getUnreadCount() + 1); // 안읽은 메시지 1 증가
        receiverRoom.setUpdated(now);

        // 6. 저장
        chatRoomRepository.save(senderRoom);
        chatRoomRepository.save(receiverRoom);
        System.out.println("채팅방 메타데이터 업데이트 완료");
    }

}