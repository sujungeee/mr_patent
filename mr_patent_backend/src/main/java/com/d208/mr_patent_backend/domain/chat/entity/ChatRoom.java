package com.d208.mr_patent_backend.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer chatId;

    private Integer roomId;
    private Integer userId;

    @Column(columnDefinition = "TEXT")
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead; // DB에는 TINYINT형식으로 저장됨 (0 = false ,1 = true)


}
