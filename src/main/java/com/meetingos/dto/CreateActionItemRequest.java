package com.meetingos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateActionItemRequest {

    @NotNull(message = "Meeting ID is required")
    private UUID meetingId;

    private UUID decisionId;

    @NotBlank(message = "Task is required")
    private String task;

    @NotBlank(message = "Assigned to is required")
    private String assignedTo;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
}
