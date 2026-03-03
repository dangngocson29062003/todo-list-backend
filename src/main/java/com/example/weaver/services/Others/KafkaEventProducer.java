package com.example.weaver.services.Others;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendSync(String topic, String payload) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(topic, payload).get();
    }
}