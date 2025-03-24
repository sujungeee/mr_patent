package com.d208.mr_patent_backend.domain.chat.repository;

import com.d208.mr_patent_backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    List<ChatRoom> findByUserId(Integer userId);
}
