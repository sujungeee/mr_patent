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
    private Integer roomId;

    private Integer userId;
    private Integer status;
    private String unreadcount;
    private LocalDateTime created;
    private LocalDateTime updated;

}
