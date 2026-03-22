package com.example.weaver.repositories;

import com.example.weaver.dtos.responses.ProjectSimpleResponse;
import com.example.weaver.models.ProjectMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Optional<ProjectMember> findByProject_IdAndUser_Id(UUID projectId, UUID userId);

    @EntityGraph(attributePaths = {"project"})
    Optional<ProjectMember> findWithProjectByProject_IdAndUser_Id(UUID projectId, UUID userId);

    List<ProjectMember> findAllByProject_Id(UUID projectId);

    @Query("""
                SELECT new com.example.weaver.dtos.responses.ProjectSimpleResponse(
                    pm.project.id,
                    pm.project.name,
                    pm.project.avatarUrl,
                    pm.isPinned,
                    pm.lastAccess,
                    pm.createdAt
                )
                FROM ProjectMember pm
                WHERE pm.user.id = :userId
                  AND pm.isPinned = true
                ORDER BY
                          pm.lastAccess DESC NULLS FIRST,
                          pm.createdAt DESC
            """)
    List<ProjectSimpleResponse> findPinnedProject(UUID userId);

    @Query("""
                SELECT new com.example.weaver.dtos.responses.ProjectSimpleResponse(
                    pm.project.id,
                    pm.project.name,
                    pm.project.avatarUrl,
                    pm.isPinned,
                    pm.lastAccess,
                    pm.createdAt
                )
                FROM ProjectMember pm
                WHERE pm.user.id = :userId
                  AND (pm.isPinned = false OR pm.isPinned IS NULL)
                ORDER BY
                          pm.lastAccess DESC NULLS FIRST,
                          pm.createdAt DESC
            """)
    Slice<ProjectSimpleResponse> findUnpinnedProject(UUID userId, Pageable pageable);


    @Query("""
                SELECT new com.example.weaver.dtos.responses.ProjectSimpleResponse(
                    pm.project.id,
                    pm.project.name,
                    pm.project.avatarUrl,
                    pm.isPinned,
                    pm.lastAccess,
                    pm.createdAt
                )
                FROM ProjectMember pm
                WHERE pm.user.id = :userId
                     AND (pm.isPinned = false OR pm.isPinned IS NULL)
                     AND (
                           (pm.lastAccess IS NOT NULL
                              AND (
                                  pm.lastAccess < :lastAccess
                                  OR (pm.lastAccess = :lastAccess AND pm.createdAt < :lastCreatedAt)
                                  )
                           )
                           OR (
                               pm.lastAccess IS NULL
                               AND :lastAccess IS NOT NULL
                              )
                         )
                ORDER BY
                     pm.lastAccess DESC NULLS FIRST,
                     pm.createdAt DESC
            """)
    Slice<ProjectSimpleResponse> findUnpinnedProjectWithCursor(
            UUID userId,
            Instant lastAccess,
            Instant lastCreatedAt,
            Pageable pageable
    );

    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);

    @EntityGraph(attributePaths = {
            "user"
    })
    List<ProjectMember> findWithUsersByProject_Id(UUID projectId);

    List<ProjectMember> findAllByUser_IdAndIsPinnedTrue(UUID userId);

    long countByProject_IdAndUser_Id(UUID projectId, UUID userId);

}
