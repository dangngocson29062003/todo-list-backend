package com.example.weaver.repositories;

import com.example.weaver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("""
    SELECT u FROM users u 
    WHERE u.id != :currentUserId 
    AND (LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) 
         OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :query, '%')))
""")
    List<User> searchUsersExcludingSelf(
            @Param("currentUserId") UUID currentUserId,
            @Param("query") String query
    );
}
