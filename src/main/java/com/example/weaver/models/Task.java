package com.example.weaver.models;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column
    private Instant startedAt;

    @Column
    private Instant endedAt;

    @Column
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId")
    private Task parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Task> children = new HashSet<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private Set<Attachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private Set<TaskAssignment> assignments = new HashSet<>();
}
