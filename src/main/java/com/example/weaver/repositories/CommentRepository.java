package com.example.weaver.repositories;

import com.example.weaver.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
                SELECT c.task.id, COUNT(c)
                FROM comments c
                WHERE c.task.id IN :taskIds
                GROUP BY c.task.id
            """)
    List<Object[]> countByTaskIds(List<UUID> taskIds);

    List<Comment> findCommentsByTask_Id(UUID taskId);
}
