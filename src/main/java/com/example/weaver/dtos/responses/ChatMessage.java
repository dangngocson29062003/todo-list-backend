package com.example.weaver.dtos.responses;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
        private UUID projectId;
        private String sender;
        private String content;
        private LocalDateTime timestamp;

        // getters setters

}
