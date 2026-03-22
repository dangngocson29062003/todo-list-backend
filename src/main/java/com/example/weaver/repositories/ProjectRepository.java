package com.example.weaver.repositories;

import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("""
        SELECT DISTINCT p
        FROM projects p
        LEFT JOIN FETCH p.members m
        LEFT JOIN FETCH m.user
        LEFT JOIN FETCH p.createdBy
        WHERE EXISTS (
            SELECT 1 FROM ProjectMember pm
            WHERE pm.project = p AND pm.user.id = :userId
        )
    """)
    List<Project> findAllProjectsByMembers(ProjectMember projectMember);
}
