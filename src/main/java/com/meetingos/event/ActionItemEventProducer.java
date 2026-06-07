package com.meetingos.event;

import com.meetingos.config.MetricsConfig;
import com.meetingos.entity.ActionItem;
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
public class ActionItemEventProducer {

    private static final String TOPIC = "action-events";
    private final KafkaTemplate<String, MeetingOsEvent> kafkaTemplate;
    private final MetricsConfig metrics;

    public void sendActionItemCreated(ActionItem item) {
        send("ACTION_ITEM_CREATED", item);
    }

    public void sendActionItemCompleted(ActionItem item) {
        send("ACTION_ITEM_COMPLETED", item);
    }

    public void sendActionItemOverdue(ActionItem item) {
        send("ACTION_ITEM_OVERDUE", item);
    }

    private void send(String eventType, ActionItem a) {
        MeetingOsEvent event = MeetingOsEvent.builder()
                .eventType(eventType)
                .entityId(a.getId())
                .entityType("ActionItem")
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .payload(Map.of(
                        "task", a.getTask(),
                        "assignedTo", a.getAssignedTo(),
                        "meetingId", a.getMeetingId().toString(),
                        "status", a.getStatus().name(),
                        "dueDate", a.getDueDate().toString()
                ))
                .build();

        kafkaTemplate.send(TOPIC, a.getId().toString(), event);
        metrics.kafkaCounter(eventType).increment();
        log.info("Published {} for action item {}", eventType, a.getId());
    }
}
