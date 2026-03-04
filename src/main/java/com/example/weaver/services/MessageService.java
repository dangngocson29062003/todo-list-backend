package com.example.weaver.services;

import com.example.weaver.dtos.requests.MessageRequest;
import com.example.weaver.dtos.responses.MessageResponse;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Message;
import com.example.weaver.models.Project;
import com.example.weaver.models.User;
import com.example.weaver.repositories.MessageRepository;
import com.example.weaver.repositories.ProjectMemberRepository;
import com.example.weaver.repositories.ProjectRepository;
import com.example.weaver.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public void sendMessage(MessageRequest request, UUID sender) {

        User user = userRepository.findById(sender)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(() -> new NotFoundException("Project not found"));

        boolean isMember = projectMemberRepository
                .existsByProject_IdAndUser_Id(
                        request.getProjectId(),
                        user.getId()
                );

        if (!isMember) {
            throw new RuntimeException("You are not a member of this projectResponse");
        }

        Message message = Message.builder()
                .content(request.getContent())
                .project(project)
                .sender(user)
                .build();

        messageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/project/" + request.getProjectId(),
                request
        );
    }
    public List<MessageResponse> getMessages(UUID projectId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));

        boolean isMember = projectMemberRepository
                .existsByProject_IdAndUser_Id(
                        projectId,
                        user.getId()
                );
        if (!isMember) {
            throw new RuntimeException("You are not a member of this projectResponse");
        }
        List<MessageResponse> response = messageRepository.findMessagesByProject_Id(projectId).stream().map(MessageResponse::toResponse).toList();
        return response;
    }
}
