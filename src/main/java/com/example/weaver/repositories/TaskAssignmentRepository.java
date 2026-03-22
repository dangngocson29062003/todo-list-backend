package com.example.weaver.repositories;

import com.example.weaver.dtos.responses.TaskSimpleResponse;
import com.example.weaver.models.Task;
import com.example.weaver.models.TaskAssignment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findAllByTask_Id(Long taskId);
    boolean existsByTask_IdAndUser_Id(Long taskId, UUID userId);
    Optional<TaskAssignment> findTaskAssignmentByTask_IdAndUser_Id(Long taskId, UUID userId);

    @Query("""
        SELECT new com.example.weaver.dtos.responses.TaskSimpleResponse(
                    ts.task.id,
                    ts.task.name,
                    ts.task.status,
                    ts.taskIndex,
                    ts.isPinned
                )
        FROM TaskAssignment ts
        LEFT JOIN tasks t
        ON ts.task.id=t.id
        WHERE ts.user.id=:userId
                AND ts.isPinned=TRUE
                AND ts.task.parent IS NULL
        ORDER BY ts.taskIndex ASC
""")
    List<TaskSimpleResponse> findPinnedTask(UUID userId);

    @Query("""
        SELECT new com.example.weaver.dtos.responses.TaskSimpleResponse(
                    ts.task.id,
                    ts.task.name,
                    ts.task.status,
                    ts.taskIndex,
                    ts.isPinned
                )
        FROM TaskAssignment ts
        LEFT JOIN tasks t
        ON ts.task.id=t.id
        WHERE ts.user.id=:userId
                AND (ts.isPinned IS NULL OR ts.isPinned = FALSE)
                AND ts.task.parent IS NULL
        ORDER BY ts.taskIndex ASC
""")
    Slice<TaskSimpleResponse> findUnpinnedTask(UUID userId, Pageable pageable);

    @Query("""
                    SELECT new com.example.weaver.dtos.responses.TaskSimpleResponse(
                                ts.task.id,
                                ts.task.name,
                                ts.task.status,
                                ts.taskIndex,
                                ts.isPinned
                            )
                    FROM TaskAssignment ts
                    LEFT JOIN tasks t
                    ON ts.task.id=t.id
                    WHERE ts.user.id=:userId
                            AND (ts.isPinned IS NULL OR ts.isPinned = FALSE)
                            AND (:cursor IS NULL OR ts.taskIndex<:cursor)
                            AND ts.task.parent IS NULL
                    ORDER BY ts.taskIndex ASC
            """)
    Slice<TaskSimpleResponse> findUnpinnedTaskWithCursor(UUID userId,Integer cursor,Pageable pageable);
}
