package com.example.weaver.dtos.requests;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProjectRequest {
    private String name;
    private String description;
    private String tags;
    private Stage stage;
    private Priority priority;
    private List<String> goals;
    private List<String> techStack;
    private String githubUrl;
    private String figmaUrl;
    private Instant startDate;
    private Instant endDate;
}
