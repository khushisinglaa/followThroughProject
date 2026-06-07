package com.meetingos.event;

import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingOsEvent {
    private String eventType;
    private UUID entityId;
    private String entityType;
    private Map<String, Object> payload;
    private Instant timestamp;
    private String correlationId;
}
