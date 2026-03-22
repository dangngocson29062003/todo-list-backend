package com.example.weaver.repositories;

import com.example.weaver.models.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @EntityGraph(attributePaths = {
            "createdBy",
            "members",
            "members.user"
    })
    @Query("SELECT p FROM projects p WHERE p.id = :id")
    Optional<Project> getWithCreatedByAndMembersData(UUID id);
}
