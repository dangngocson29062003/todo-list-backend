package com.example.weaver.models;

import com.example.weaver.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "project_members",
        indexes = {
                @Index(name = "idx_project_member_project_id", columnList = "project_id"),
                @Index(name = "idx_project_member_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                //Auto create composite index for projectId and userId
                @UniqueConstraint(
                        name = "uk_project_member_project_user",
                        columnNames = {"project_id", "user_id"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
