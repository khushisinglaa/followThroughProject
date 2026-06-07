package com.meetingos.dto;

import com.meetingos.entity.DecisionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDecisionRequest {

    @NotNull(message = "Meeting ID is required")
    private UUID meetingId;

    @NotBlank(message = "Title is required")
    private String title;

    private String reason;
    private List<String> alternatives;
    private List<String> tradeoffs;
    private String owner;

    @NotNull(message = "Status is required")
    private DecisionStatus status;
}
