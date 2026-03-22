package com.example.weaver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    private String description;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private Instant finishedAt;

    @Column(columnDefinition = "json")
    private List<String> links;

    @Column(columnDefinition = "json")
    private List<String> tags;

    @OneToMany(mappedBy = "project")
    private Set<ProjectMember> members=new HashSet<>();

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
