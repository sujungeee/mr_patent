package com.d208.mr_patent_backend.domain.chat.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatListDto {
    private Integer roomId;
    private String lastMessage;
    private LocalDateTime lastTimestamp;
}
