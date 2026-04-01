package com.example.weaver.services;

import com.example.weaver.dtos.requests.CommentRequest;
import com.example.weaver.dtos.responses.CommentResponse;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Comment;
import com.example.weaver.models.Task;
import com.example.weaver.models.User;
import com.example.weaver.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment create(Task task, User user, CommentRequest request) {
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId()).orElseThrow(() -> new NotFoundException("Comment's parent not found"));
        }
        Comment comment = Comment.builder().content(request.getContent()).task(task).user(user).parent(parent).build();
        return commentRepository.save(comment);
    }

    public List<Comment> getComments(UUID taskId) {
        return commentRepository.findCommentsByTask_Id(taskId);
    }

    public Comment update(Comment comment, String content) {
        comment.setContent(content);
        return comment;
    }

    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }

    public Comment checkAuthor(Long id, UUID userId) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new NotFoundException("Comment not found"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new BadRequestException("You are not the author of this comment");
        }
        return comment;
    }

    public Map<UUID, Long> getCommentCountMap(List<UUID> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Map.of();
        }

        return commentRepository.countByTaskIds(taskIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }

}
