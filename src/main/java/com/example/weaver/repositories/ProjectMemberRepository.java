package com.example.weaver.repositories;

import com.example.weaver.enums.Role;
import com.example.weaver.models.ProjectMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);

    @Query("SELECT pm.role FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    Optional<Role> findRoleByProjectIdAndUserId(UUID projectId, UUID userId);


    @EntityGraph(attributePaths = {
            "user"
    })
    List<ProjectMember> findWithUsersByProject_Id(UUID projectId);


    long countByUser_IdAndIsPinnedTrue(UUID userId);

    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.lastAccess = :now " +
            "WHERE pm.user.id = :userId AND pm.project.id = :projectId")
    void updateLastAccess(UUID userId,
                          UUID projectId,
                          Instant now);

    @Modifying
    @Query("""
        UPDATE ProjectMember pm
        SET pm.isFavorited = :isFavorited
        WHERE pm.user.id = :userId
          AND pm.project.id = :projectId
    """)
    int updateFavorite(
            @Param("userId") UUID userId,
            @Param("projectId") UUID projectId,
            @Param("isFavorited") boolean isFavorited
    );

}
