package com.example.weaver.models;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity(name = "projects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String name;
    @Column
    private String description;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private Instant finishedAt;
    @Column
    private String tags;
    @Column
    @Enumerated(EnumType.STRING)
    private Stage stage;
    @Column
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(nullable = false, columnDefinition = "TIMESTAMP(6) DEFAULT NOW()")
    private LocalDate startDate;
    @Column(nullable = false, columnDefinition = "TIMESTAMP(6) DEFAULT NOW()")
    private LocalDate endDate;

    @Column(columnDefinition = "text[]")
    private List<String> goals;

    @Column(columnDefinition = "text[]")
    private List<String> techStack;

    @Column
    private String githubUrl;
    @Column
    private String figmaUrl;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private Set<ProjectMember> members;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private Set<Task> tasks;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Message> messages;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deletedBy")
    private User deletedBy;
}
