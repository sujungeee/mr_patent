package com.d208.mr_patent_backend.domain.chat.entity;

//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer chatId;

    private String roomId;
    private Integer userId;

    @Column(columnDefinition = "TEXT")
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead; // DB에는 TINYINT형식으로 저장됨 (0 = false ,1 = true)
}
