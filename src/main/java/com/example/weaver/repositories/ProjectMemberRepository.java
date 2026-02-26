package com.example.weaver.repositories;

import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Optional<ProjectMember> findByProject_IdAndUser_Id(UUID projectId, UUID userId);

    List<ProjectMember> findAllByProject_Id(UUID projectId);

    @Query("""
                SELECT pm.project
                FROM ProjectMember pm
                WHERE pm.user.id=:userId
            """)
    List<Project> findProjectsByUserId(UUID userId);

    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);
}
