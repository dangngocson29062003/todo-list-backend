package com.example.weaver.services.Others;

import com.example.weaver.dtos.events.MemberAddedEvent;
import com.example.weaver.dtos.events.TaskAssignedEvent;
import com.example.weaver.services.AppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final ObjectMapper objectMapper;
    private final AppService appService;

    @KafkaListener(topics = "MemberAdded")
    public void handleMemberAdded(String payload) throws Exception {
        MemberAddedEvent event = objectMapper.readValue(payload, MemberAddedEvent.class);
        appService.createNotifications(List.of(event.userId()), event.code(),
                event.projectResponse(), event.category(),event.priority(), event.type());

//        System.out.println("Received NotificationCreatedEvent : " + event);
    }
    @KafkaListener(topics = "TaskAssigned")
    public void handleTaskAssigned(String payload) throws Exception {
        TaskAssignedEvent event = objectMapper.readValue(payload, TaskAssignedEvent.class);
        appService.createNotifications(event.userIds(), event.code(),
                event.taskResponse(), event.category(),event.priority(), event.type());

//        System.out.println("Received NotificationCreatedEvent : " + event);
    }

}
