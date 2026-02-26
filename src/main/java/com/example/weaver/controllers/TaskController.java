package com.example.weaver.controllers;

import com.example.weaver.dtos.responses.TaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class TaskController {

    @GetMapping
    public TaskResponse getTasks() {
        return new TaskResponse();
    }
}
