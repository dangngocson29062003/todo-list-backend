package com.example.weaver.models;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity(name = "projects")
@Data
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

    @Column
    private String tags;
    @Column
    @Enumerated(EnumType.STRING)
    private Stage stage;
    @Column
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(nullable = false, columnDefinition = "TIMESTAMP(6) DEFAULT NOW()")
    private Instant startDate;
    @Column(nullable = false, columnDefinition = "TIMESTAMP(6) DEFAULT NOW()")
    private Instant endDate;

    @Column(columnDefinition = "text[]")
    private List<String> goals;

    @Column(columnDefinition = "text[]")
    private List<String> techStack;

    @Column
    private String githubUrl;
    @Column
    private String figmaUrl;

    @OneToMany(mappedBy = "project")
    private Set<ProjectMember> members;

    @OneToMany(mappedBy = "project")
    private Set<Task> tasks;

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private Set<Message> messages;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
