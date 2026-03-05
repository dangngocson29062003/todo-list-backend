package com.example.weaver.dtos.responses;

import com.example.weaver.models.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Instant createdAt;
    private Long parentId;

    public static CommentResponse toResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getContent(), comment.getCreatedAt(), comment.getParent() != null ? comment.getParent().getId() : null);
    }
}
