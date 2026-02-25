package com.example.weaver.services;

import com.example.weaver.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskService {
    @Autowired
    TaskRepository taskRepository;


}
