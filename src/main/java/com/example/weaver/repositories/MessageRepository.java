package com.example.weaver.repositories;

import com.example.weaver.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Limit 20
    List<Message> findMessagesByProject_Id(UUID projectId);
}
