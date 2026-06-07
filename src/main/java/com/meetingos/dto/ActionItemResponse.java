package com.meetingos.dto;

import com.meetingos.entity.ActionItemStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionItemResponse {
    private UUID id;
    private UUID meetingId;
    private UUID decisionId;
    private String task;
    private String assignedTo;
    private LocalDate dueDate;
    private ActionItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
