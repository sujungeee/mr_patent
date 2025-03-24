package com.d208.mr_patent_backend.domain.chat.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {
    private Integer roomId;            // 채팅방 ID
    private Integer userId;            // 보낸 사람 ID
    private Integer status;            // 채팅방 상태 ID
    private Integer unreadcount;       // 안읽은 메세지 내용
    private LocalDateTime created;     // 생성일자 시간
    private LocalDateTime updated;     // 수정일자 여부

}

