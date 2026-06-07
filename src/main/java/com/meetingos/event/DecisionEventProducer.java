package com.meetingos.event;

import com.meetingos.config.MetricsConfig;
import com.meetingos.entity.Decision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionEventProducer {

    private static final String TOPIC = "decision-events";
    private final KafkaTemplate<String, MeetingOsEvent> kafkaTemplate;
    private final MetricsConfig metrics;

    public void sendDecisionCreated(Decision decision) {
        send("DECISION_CREATED", decision);
    }

    public void sendDecisionUpdated(Decision decision) {
        send("DECISION_UPDATED", decision);
    }

    private void send(String eventType, Decision d) {
        MeetingOsEvent event = MeetingOsEvent.builder()
                .eventType(eventType)
                .entityId(d.getId())
                .entityType("Decision")
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .payload(Map.of(
                        "title", d.getTitle(),
                        "meetingId", d.getMeetingId().toString(),
                        "owner", d.getOwner() != null ? d.getOwner() : "",
                        "status", d.getStatus().name()
                ))
                .build();

        kafkaTemplate.send(TOPIC, d.getId().toString(), event);
        metrics.kafkaCounter(eventType).increment();
        log.info("Published {} for decision {}", eventType, d.getId());
    }
}
