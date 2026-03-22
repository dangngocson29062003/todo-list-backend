package com.example.weaver.repositories;

import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Query(value = """
        SELECT DISTINCT p
        FROM projects p
        LEFT JOIN FETCH p.members m
        WHERE m.user.id = :userId
""")
    Object findProjectsByUserId(UUID userId);

    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);
}
