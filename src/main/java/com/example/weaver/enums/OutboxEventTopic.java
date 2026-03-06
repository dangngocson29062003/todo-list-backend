package com.example.weaver.enums;

import lombok.Getter;

@Getter
public enum OutboxEventTopic {
    MEMBER_ADDED("member.added"),
    MEMBER_UPDATED("member.updated"),
    MEMBER_REMOVED("member.removed"),
    MEMBER_ROLE_UPDATED("member.role.updated"),
    TASK_ASSIGNED("task.assigned"),
    NOTIFICATION_CREATED("notification.created");

    private final String topic;
    OutboxEventTopic(String topic) {
        this.topic = topic;
    }
}
