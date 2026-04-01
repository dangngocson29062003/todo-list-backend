package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.responses.FileResponse;
import com.example.weaver.services.AppService;
import com.example.weaver.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tasks/{taskId}/files")
@RequiredArgsConstructor
public class FileController {

    private final AppService appService;

    @GetMapping
    public List<FileResponse> getFiles(@PathVariable UUID taskId, @AuthenticationPrincipal AuthUser authUser) {
        return appService.getFiles(taskId, authUser.getId());
    }

    @PostMapping
    public FileResponse upload(@PathVariable UUID taskId, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal AuthUser authUser) {
        return appService.uploadFiles(taskId, authUser.getId(), file);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID taskId, @PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser) {
        appService.deleteFile(taskId, authUser.getId(), id);
    }
}
