package com.example.weaver.dtos.requests;

import com.example.weaver.enums.Priority;
import com.example.weaver.enums.Stage;
import com.example.weaver.models.Message;
import com.example.weaver.models.Task;
import com.example.weaver.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequest {

    @NotEmpty
    private String name;
    private String description;

    private String tags;


    private Stage stage;
    private Priority priority;

    private List<String> goals;
    private List<String> techStack;

    private String githubUrl;
    private String figmaUrl;

    @NotEmpty
    private Instant startDate;
    @NotEmpty
    private Instant endDate;
    private Instant createdAt;
    private Instant updatedAt;

    private User createdBy;
}
