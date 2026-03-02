package com.example.weaver.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
        private UUID projectId;
        private String sender;
        private String content;
        private LocalDateTime timestamp;

        // getters setters

}
