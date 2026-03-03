package com.example.weaver.repositories;

import com.example.weaver.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findMessagesByProject_Id(UUID projectId);
}
