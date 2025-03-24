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
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer roomId;

    private Integer userId;
    private Integer status;
    private Integer unreadcount;

    @Column(columnDefinition = "TEXT")
    private String lastMessage;
    private LocalDateTime lastTimestamp;
    private LocalDateTime created;
    private LocalDateTime updated;

}
