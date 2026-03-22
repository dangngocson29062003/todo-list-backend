package com.example.weaver.repositories;

import com.example.weaver.dtos.responses.ProjectSummaryResponse;
import com.example.weaver.models.Project;
import com.example.weaver.models.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("""
                SELECT new com.example.weaver.dtos.responses.ProjectSummaryResponse(
                    p.id,
                    p.name,
                    p.description,
                    p.stage,
                    p.priority,
                    p.tags,
                    p.techStack,
                    p.startDate,
                    p.endDate,
                    
                    (SELECT COUNT(pm.id) FROM ProjectMember pm WHERE pm.project = p),
            
                    (SELECT COUNT(t.id) FROM tasks t WHERE t.project = p),
            
                    (SELECT COUNT(t2.id) FROM tasks t2 
                        WHERE t2.project = p 
                        AND t2.status = 'DONE'
                    ),
            
                    u.id,
                    u.email,
                    u.fullName,
                    u.avatarUrl
                )
                FROM projects p
                JOIN p.createdBy u
                WHERE EXISTS (
                    SELECT 1 FROM ProjectMember pm2
                    WHERE pm2.project = p AND pm2.user.id = :userId
                )
            """)
    List<ProjectSummaryResponse> findAllSummariesByUserId(UUID userId);

    @Query("""
                SELECT DISTINCT p
                FROM projects p
                LEFT JOIN FETCH p.createdBy
                LEFT JOIN FETCH p.members m
                LEFT JOIN FETCH m.user
                WHERE p.id = :projectId
                AND EXISTS (
                            SELECT 1 FROM ProjectMember pm
                            WHERE pm.project = p AND pm.user.id = :userId
                           )
            """)
    Optional<Project> findDetailById(UUID projectId, UUID userId);
}
