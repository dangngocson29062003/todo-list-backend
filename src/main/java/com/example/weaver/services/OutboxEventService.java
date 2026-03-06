package com.example.weaver.services;

import com.example.weaver.enums.OutboxEventStatus;
import com.example.weaver.enums.OutboxEventTopic;
import com.example.weaver.exceptions.AppException;
import com.example.weaver.models.OutboxEvent;
import com.example.weaver.repositories.OutboxEventRepository;
import com.example.weaver.services.Others.KafkaEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxEventRepository repository;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    public void create(OutboxEventTopic topic, Object payloadObject){
        OutboxEvent outboxEvent= null;
        try {
            outboxEvent = OutboxEvent.builder()
                    .topic(topic)
                    .payload(objectMapper.writeValueAsString(payloadObject))
                    .status(OutboxEventStatus.PENDING)
                    .build();
        } catch (JsonProcessingException e) {
            throw new AppException(500,"OUTBOX_PAYLOAD_ERROR","Error when convert Object to Json");
        }
        repository.save(outboxEvent);
    }


    public void processPendingEvents() {
        List<OutboxEvent> events = repository.fetchTop50Pending();
        for (OutboxEvent event : events) {
            processSingleEvent(event);
        }
    }
    public void processSingleEvent(OutboxEvent event) {
        try {
            kafkaEventProducer.sendSync(
                    event.getTopic().getTopic(),
                    event.getPayload()
            );

            event.setStatus(OutboxEventStatus.SENT);
            event.setSentAt(Instant.now());

        } catch (Exception e) {

            event.setRetryCount(event.getRetryCount() + 1);

            if (event.getRetryCount() >= 5) {
                event.setStatus(OutboxEventStatus.FAILED);
            }
        }
    }

    public List<OutboxEvent> fetchEvents() {
        return repository.fetchTop50Pending();
    }

    public void cleanupSentEvents() {
        repository.deleteSentEventsBatch();
    }


}
