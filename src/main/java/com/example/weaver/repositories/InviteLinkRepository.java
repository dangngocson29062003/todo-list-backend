package com.example.weaver.repositories;

import com.example.weaver.models.InviteLink;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InviteLinkRepository extends JpaRepository<InviteLink,Long> {
    Optional<InviteLink> findByProject_Id(UUID projectId);

    Optional<InviteLink> findByToken(String token);

    @Query("""
            SELECT l.id FROM InviteLink l
            WHERE l.expiresAt < :currentTime
        """)
    List<Long> findExpiredIds(Instant currentTime, Pageable pageable);

    @Modifying
    @Query("""
    DELETE FROM InviteLink l
    WHERE l.id IN :ids
""")
    void deleteByIds(List<Long> ids);
}
