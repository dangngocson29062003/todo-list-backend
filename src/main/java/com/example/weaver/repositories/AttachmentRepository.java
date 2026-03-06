package com.example.weaver.repositories;

import com.example.weaver.models.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findAttachmentsByTask_Id(Long taskId);
}
