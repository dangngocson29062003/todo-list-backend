package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.requests.CommentRequest;
import com.example.weaver.dtos.responses.CommentResponse;
import com.example.weaver.services.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final AppService appService;

    @PostMapping
    public CommentResponse create(@PathVariable Long taskId, @RequestBody CommentRequest commentRequest, @AuthenticationPrincipal AuthUser authUser) {
        return appService.createComment(taskId, authUser.getId(), commentRequest);
    }

    @GetMapping
    public List<CommentResponse> getComments(@PathVariable Long taskId, @AuthenticationPrincipal AuthUser authUser) {
        return appService.getComments(taskId, authUser.getId());
    }

    @PutMapping ("/{id}")
    public CommentResponse update(@PathVariable Long taskId, @PathVariable Long id, @RequestBody String content, @AuthenticationPrincipal AuthUser authUser) {
        return appService.updateComment(taskId, id, authUser.getId(), content);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long taskId, @PathVariable Long id, @AuthenticationPrincipal AuthUser authUser){
        appService.deleteComment(taskId, authUser.getId(), id);
    }
}
