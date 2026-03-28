package com.example.weaver.models;

import com.example.weaver.enums.InviteType;
import com.example.weaver.enums.MemberStatus;
import com.example.weaver.enums.Role;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "project_members",
        indexes = {
//                @Index(name = "idx_project_member_project_id", columnList = "project_id"),
                @Index(
                        name = "idx_user_id_last_access_created_at_index",
                        columnList = "user_id,last_access DESC, created_at DESC"
                )
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_pinned", columnDefinition = "boolean DEFAULT false")
    private boolean isPinned;

    @Column(name = "last_access")
    @CreationTimestamp
    private Instant lastAccess;

    @Column(name = "is_favorited", columnDefinition = "boolean DEFAULT false")
    private boolean isFavorited;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_type")
    private InviteType inviteType;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
