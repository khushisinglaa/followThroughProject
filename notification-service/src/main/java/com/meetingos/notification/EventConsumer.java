package com.meetingos.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2),
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR
    )
    @KafkaListener(topics = "decision-events", groupId = "notification-service")
    public void handleDecisionEvent(MeetingOsEvent event) {
        log.info("📋 DECISION EVENT | type={} | id={}", event.getEventType(), event.getEntityId());
        messagingTemplate.convertAndSend("/topic/decisions", event);
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2),
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR
    )
    @KafkaListener(topics = "action-events", groupId = "notification-service")
    public void handleActionItemEvent(MeetingOsEvent event) {
        if ("ACTION_ITEM_OVERDUE".equals(event.getEventType())) {
            var p = event.getPayload();
            log.warn("⚠️ [OVERDUE] Task: {} | Assigned to: {} | Was due: {}",
                    p.get("task"), p.get("assignedTo"), p.get("dueDate"));
            messagingTemplate.convertAndSend("/topic/overdue", event);
        } else {
            log.info("✅ ACTION EVENT | type={} | id={}", event.getEventType(), event.getEntityId());
            messagingTemplate.convertAndSend("/topic/actions", event);
        }
    }

    @DltHandler
    public void handleDlt(MeetingOsEvent event) {
        log.error("💀 DLQ | Failed after retries | type={} | id={}", event.getEventType(), event.getEntityId());
    }
}
