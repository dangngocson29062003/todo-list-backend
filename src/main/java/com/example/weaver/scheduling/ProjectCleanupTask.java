package com.example.weaver.scheduling;

import com.example.weaver.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class ProjectCleanupTask {
    private final ProjectRepository projectRepository;

    @Scheduled(cron = "0 0 3 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void autoHardDeleteOldProjects() {
        Instant threshold = Instant.now().minus(1, ChronoUnit.MINUTES);
        try {
            projectRepository.hardDeleteOldSoftDeletedProjects(threshold);
        } catch (Exception e) {

        }
    }
}
