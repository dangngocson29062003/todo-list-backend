package com.example.weaver.repositories;

import com.example.weaver.models.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findAllByTask_Id(Long taskId);
    boolean existsByTask_IdAndUser_Id(Long taskId, UUID userId);
    Optional<TaskAssignment> findTaskAssignmentByTask_IdAndUser_Id(Long taskId, UUID userId);
}
