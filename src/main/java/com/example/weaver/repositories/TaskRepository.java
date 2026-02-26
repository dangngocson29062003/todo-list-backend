package com.example.weaver.repositories;

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
    SELECT t FROM tasks t
    WHERE t.project.id = :projectId
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

    Task findByIdAndProject_Id(Long taskId, UUID projectId);
}
