package com.example.weaver.dtos.events;

import java.util.List;
import java.util.UUID;

public record NotificationCreatedEvent(Long notificationId, List<UUID> userIds) {
}
