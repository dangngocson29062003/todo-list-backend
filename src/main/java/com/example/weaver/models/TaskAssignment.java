package com.example.weaver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity()
@Table(
        name = "task_assignments",
        indexes = {
                @Index(name = "idx_task_assignment_user_id_last_access", columnList = "user_id,last_access")
        }
)
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taskId", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignedBy", nullable = false)
    private User assignedBy;

    @Column(name = "task_index")
    private Integer taskIndex;

    @Column(name = "is_pinned")
    private Boolean isPinned=false;

    @Column(name = "last_access")
    private Instant lastAccess;

    @Column(name = "is_favorited")
    private Boolean isFavorited;

    @CreationTimestamp
    private Instant assignedAt;

}
