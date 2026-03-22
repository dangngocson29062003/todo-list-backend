package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.responses.*;
import com.example.weaver.services.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final AppService appService;

    @GetMapping("/home")
    public HomeResponse getHomeData(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Integer limit) {
        ProjectSimpleResponses projectsData=
                appService.getProjectsByUserId(authUser.getId(),null,null,limit);
        TaskSimpleResponses tasksData =appService.getAssignedTasks(authUser.getId(),null,limit);
        return new HomeResponse(projectsData, tasksData);
    }
}
