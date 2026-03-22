package com.example.weaver.repositories;

import com.example.weaver.enums.Role;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Optional<ProjectMember> findByProject_IdAndUser_Id(UUID projectId, UUID userId);

    @EntityGraph(attributePaths = {"project"})
    Optional<ProjectMember> findWithProjectByProject_IdAndUser_Id(UUID projectId, UUID userId);

    List<ProjectMember> findAllByProject_Id(UUID projectId);

    List<ProjectMember> findByUser_Id(UUID userId);

    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);

    @Query("SELECT pm.role FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    Optional<Role> findRoleByProjectIdAndUserId(UUID projectId, UUID userId);

}
