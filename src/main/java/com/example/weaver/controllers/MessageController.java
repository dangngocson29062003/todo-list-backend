package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.responses.MessageResponse;
import com.example.weaver.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messages/{projectId}")
public class MessageController {

    private final MessageService messageService;

    @GetMapping()
    public List<MessageResponse> getMessages(@PathVariable UUID projectId, @AuthenticationPrincipal AuthUser authUser) {
        return messageService.getMessages(projectId, authUser.getId());
    }
}
