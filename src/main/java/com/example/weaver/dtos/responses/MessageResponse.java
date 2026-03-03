package com.example.weaver.dtos.responses;

import com.example.weaver.models.Message;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

    private Long id;
    private String content;
    private UUID projectId;
    private UUID senderId;
    private Instant sentAt;

    public static MessageResponse toResponse(Message message) {
        return new MessageResponse(message.getId(), message.getContent(), message.getProject().getId(), message.getSender().getId(), message.getCreatedAt());
    }
}
