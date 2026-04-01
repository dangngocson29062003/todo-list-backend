package com.example.weaver.repositories;

import com.example.weaver.dtos.responses.DeletedProjectResponse;
import com.example.weaver.dtos.responses.ProjectSummaryResponse;
import com.example.weaver.models.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import com.example.weaver.models.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @EntityGraph(attributePaths = {
            "createdBy",
            "members",
            "members.user"
    })
    @Query("SELECT p FROM projects p WHERE p.id = :id")
    Optional<Project> getWithCreatedByAndMembersData(UUID id);

    @Query("""
    SELECT new com.example.weaver.dtos.responses.ProjectSummaryResponse(
        p.id, p.name, p.description, p.stage, p.priority, p.tags, p.techStack,
        p.startDate, p.endDate, p.createdAt,
        (SELECT COUNT(m) FROM ProjectMember m WHERE m.project = p),
        (SELECT COUNT(t) FROM tasks t WHERE t.project = p),
        (SELECT COUNT(t2) FROM tasks t2 WHERE t2.project = p AND t2.status = 'DONE'),
        pm.lastAccess,
        pm.isFavorited,
        u.id, u.email, u.fullName, u.avatarUrl
    )
    FROM projects p
    JOIN p.members pm
    JOIN p.createdBy u
    WHERE pm.user.id = :userId AND p.isDeleted = false
    AND (:name IS NULL OR p.name ILIKE CONCAT('%', CAST(:name as string), '%'))
    AND (:favorite IS NULL OR pm.isFavorited = :favorite)
""")
    Slice<ProjectSummaryResponse> findAllSummaries(
            UUID userId,
            @Param("name") String name,
            @Param("favorite") Boolean favorite,
            Pageable pageable
    );
    @Query("""
    SELECT new com.example.weaver.dtos.responses.DeletedProjectResponse(
        p.id, p.name, p.description, p.isDeleted, p.deletedAt,
        u.id, u.email, u.fullName, u.nickname, u.phone, u.address, u.avatarUrl
    )
    FROM projects p
    JOIN p.members pm
    LEFT JOIN p.deletedBy u
    WHERE pm.user.id = :userId AND p.isDeleted = true
""")
    List<DeletedProjectResponse> findAllDeletedSummaries(UUID userId);

    @Query("""
                SELECT DISTINCT p
                FROM projects p
                LEFT JOIN FETCH p.createdBy
                LEFT JOIN FETCH p.members m
                LEFT JOIN FETCH m.user
                WHERE p.id = :projectId AND p.isDeleted = false
                AND EXISTS (
                            SELECT 1 FROM ProjectMember pm
                            WHERE pm.project = p AND pm.user.id = :userId
                           )
            """)
    Optional<Project> findDetailById(UUID projectId, UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM projects p WHERE p.deletedAt <= :threshold")
    void hardDeleteOldSoftDeletedProjects(Instant threshold);
}
