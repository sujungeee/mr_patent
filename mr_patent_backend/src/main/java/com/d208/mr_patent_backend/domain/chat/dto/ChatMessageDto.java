package com.d208.mr_patent_backend.domain.chat.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private String chatId;             // 채팅 ID
    private String roomId;             // 채팅방 ID
    private String userId;             // 보낸 사람 ID
    private String message;            // 메시지 내용
    private LocalDateTime timestamp;   // 보낸 시간
    private String isRead;             // 읽음 여부

}