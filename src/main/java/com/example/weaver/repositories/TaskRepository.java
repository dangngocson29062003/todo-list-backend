package com.example.weaver.repositories;

import com.example.weaver.dtos.responses.StatsResponse;
import com.example.weaver.enums.Priority;
import com.example.weaver.enums.TaskStatus;
import com.example.weaver.enums.TaskType;
import com.example.weaver.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Query("""
                SELECT DISTINCT t
                FROM tasks t
                LEFT JOIN FETCH t.assignments a
                LEFT JOIN FETCH a.user
                LEFT JOIN FETCH a.assignedBy
                WHERE t.project.id = :projectId
                  AND t.parent IS NULL
                  AND (:status IS NULL OR t.status = :status)
                  AND (:priority IS NULL OR t.priority = :priority)
                  AND (:type IS NULL OR t.type = :type)
            """)
    List<Task> findByFilters(
            UUID projectId,
            TaskStatus status,
            Priority priority,
            TaskType type
    );

    @Query("""
            SELECT t FROM tasks t
            LEFT JOIN FETCH t.assignments a
            LEFT JOIN FETCH a.user
            WHERE t.id = :id
            """)
    Task findByIdWithAssignments(UUID id);

    @Query("""
                SELECT new com.example.weaver.dtos.responses.StatsResponse(
                    COALESCE(COUNT(t), 0),
                    COALESCE(SUM(CASE WHEN t.status = 'TODO' THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.status = 'REVIEW' THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.status = 'BLOCKED' THEN 1 ELSE 0 END), 0)
                )
                FROM tasks t WHERE t.project.id = :projectId
            """)
    StatsResponse getTaskStats(UUID projectId);
}
