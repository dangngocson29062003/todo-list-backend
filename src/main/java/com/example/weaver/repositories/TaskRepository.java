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
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("""
    SELECT DISTINCT t FROM tasks t
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
    SELECT new com.example.weaver.dtos.responses.StatsResponse(
        COUNT(DISTINCT t.id),
        SUM(CASE WHEN t.status = com.example.weaver.enums.TaskStatus.TODO THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = com.example.weaver.enums.TaskStatus.IN_PROGRESS THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = com.example.weaver.enums.TaskStatus.REVIEW THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = com.example.weaver.enums.TaskStatus.DONE THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status = com.example.weaver.enums.TaskStatus.IGNORE THEN 1 ELSE 0 END)
    )
    FROM tasks t
    WHERE t.project.id = :projectId
""")
    StatsResponse getTaskStats(UUID projectId);
}
