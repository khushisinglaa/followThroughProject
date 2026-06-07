package com.meetingos.dto;

import com.meetingos.entity.DecisionStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionResponse {
    private UUID id;
    private UUID meetingId;
    private String title;
    private String reason;
    private List<String> alternatives;
    private List<String> tradeoffs;
    private String owner;
    private DecisionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
