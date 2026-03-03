package com.example.weaver.repositories;

import com.example.weaver.models.OutboxEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OutboxEventRepository extends CrudRepository<OutboxEvent, Long> {
    @Query(value = """
                    SELECT *
                    FROM outbox_events
                    WHERE status = 'PENDING'
                    ORDER BY created_at
                    LIMIT 50
                    FOR UPDATE SKIP LOCKED;
            """, nativeQuery = true)
    List<OutboxEvent> fetchTop50Pending();

    @Modifying
    @Query(value = """
                DELETE FROM outbox_events
                WHERE id IN (
                    SELECT id FROM outbox_events
                    WHERE status = 'SENT'
                    ORDER BY sent_at
                    LIMIT 1000
                )
            """, nativeQuery = true)
    void deleteSentEventsBatch();
}
