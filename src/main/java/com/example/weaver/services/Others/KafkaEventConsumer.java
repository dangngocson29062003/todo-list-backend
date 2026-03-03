package com.example.weaver.services.Others;

import com.example.weaver.dtos.events.NotificationCreatedEvent;
import com.example.weaver.services.AppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final ObjectMapper objectMapper;
    private final AppService appService;

    @KafkaListener(topics = "NotificationCreated")
    public void createUserNotifications(String payload) throws Exception {
        NotificationCreatedEvent event = objectMapper.readValue(payload, NotificationCreatedEvent.class);
        appService.createUserNotifications(event.userIds(), event.notificationId());

//        System.out.println("Received NotificationCreatedEvent : " + event);
    }

}
