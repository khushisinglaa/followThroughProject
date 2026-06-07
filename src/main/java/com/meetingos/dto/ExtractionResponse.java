package com.meetingos.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractionResponse {
    private UUID meetingId;
    private int decisionsExtracted;
    private int actionItemsExtracted;
    private List<DecisionSummary> decisions;
    private List<ActionItemSummary> actionItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DecisionSummary {
        private UUID id;
        private String title;
        private String owner;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionItemSummary {
        private UUID id;
        private String task;
        private String assignedTo;
        private LocalDate dueDate;
    }
}
