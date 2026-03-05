package com.example.weaver.services;

import com.example.weaver.enums.NotificationCategory;
import com.example.weaver.enums.NotificationCode;
import com.example.weaver.enums.NotificationType;
import com.example.weaver.enums.Priority;
import com.example.weaver.exceptions.AppException;
import com.example.weaver.models.Notification;
import com.example.weaver.repositories.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public Notification create(NotificationCode code, Object payloadObject, NotificationCategory category,
                               int priorityRank, NotificationType type){
        Notification notification = null;
        try {
            notification = Notification.builder()
                    .code(code)
                    .payload(objectMapper.writeValueAsString(payloadObject))
                    .category(category)
                    .priorityRank(priorityRank)
                    .type(type)
                    .build();
        } catch (JsonProcessingException e) {
            throw new AppException(500,"NOTIFICATION_PARSE_JSON_ERROR","Error when converting object to json");
        }
        return notificationRepository.save(notification);
    }

    public boolean existsById(Long notificationId) {
        return  notificationRepository.existsById(notificationId);
    }
}
