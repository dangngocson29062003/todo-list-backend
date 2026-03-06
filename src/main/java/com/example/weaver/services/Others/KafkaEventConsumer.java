//package com.example.weaver.services.Others;
//
//import com.example.weaver.dtos.events.MemberEvent;
//import com.example.weaver.dtos.events.NotificationCreatedEvent;
//import com.example.weaver.dtos.events.TaskAssignedEvent;
//import com.example.weaver.dtos.responses.UserNotificationResponse;
//import com.example.weaver.services.AppService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class KafkaEventConsumer {
//    private final ObjectMapper objectMapper;
//    private final AppService appService;
//    private final SimpMessagingTemplate simpMessagingTemplate;
//
//    @KafkaListener(topics = "notification.created")
//    public void handleNotificationCreated(String payload) throws Exception {
//        NotificationCreatedEvent event = objectMapper.readValue(payload, NotificationCreatedEvent.class);
//        List<UUID> userIds = event.userIds();
//        List<Long> userNotificationIds = event.userNotificationIds();
//        for(int i=0;i<event.userIds().size();i++){
//            UserNotificationResponse response=new UserNotificationResponse(
//                    userNotificationIds.get(0),
//                    event.code(),
//                    event.payload(),
//                    event.category(),
//                    event.priorityRank(),
//                    event.type(),
//                    event.createdAt(),
//                    false);
//            System.out.println(userNotificationIds.get(i));
//            simpMessagingTemplate.convertAndSendToUser(userIds.get(i).toString(),
//                    "/queue/notification", response);
//        }
//    }
//
//    @KafkaListener(topics = "member.added")
//    public void handleMemberAdded(String payload) throws Exception {
//        MemberEvent event = objectMapper.readValue(payload, MemberEvent.class);
//        appService.createNotifications(List.of(event.userId()), event.code(),
//                event.projectResponse(), event.category(),event.priority(), event.type());
//    }
//    @KafkaListener(topics = "member.removed")
//    public void handleMemberUpdated(String payload) throws Exception {
//        MemberEvent event = objectMapper.readValue(payload, MemberEvent.class);
//        appService.createNotifications(List.of(event.userId()), event.code(),
//                event.projectResponse(), event.category(),event.priority(), event.type());
//    }
//
//    @KafkaListener(topics = "task.assigned")
//    public void handleTaskAssigned(String payload) throws Exception {
//        TaskAssignedEvent event = objectMapper.readValue(payload, TaskAssignedEvent.class);
//        appService.createNotifications(event.userIds(), event.code(),
//                event.taskResponse(), event.category(),event.priority(), event.type());
//    }
//
//}
