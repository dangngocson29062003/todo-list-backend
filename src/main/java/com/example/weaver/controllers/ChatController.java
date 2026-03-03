package com.example.weaver.controllers;

import com.example.weaver.dtos.responses.ChatMessage;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Project;
import com.example.weaver.models.User;
import com.example.weaver.repositories.ProjectMemberRepository;
import com.example.weaver.repositories.ProjectRepository;
import com.example.weaver.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;



@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage request, Principal principal) {
        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(() -> new NotFoundException("Project not found"));

        boolean isMember = projectMemberRepository
                .existsByProject_IdAndUser_Id(
                        request.getProjectId(),
                        user.getId()
                );

        if (!isMember) {
            throw new RuntimeException("You are not a member of this project");
        }



        messagingTemplate.convertAndSend(
                "/topic/project/" + request.getProjectId(),
                request
        );
    }
}
