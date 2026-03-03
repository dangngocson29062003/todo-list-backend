package com.example.weaver.controllers;

import com.example.weaver.dtos.requests.MessageRequest;
import com.example.weaver.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;


@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageRequest request, Principal principal) {
        messageService.sendMessage(request, UUID.fromString(principal.getName()));
    }
}
