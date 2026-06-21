package com.meetingos.service;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetingos.config.MetricsConfig;
import com.meetingos.dto.ExtractionResponse;
import com.meetingos.entity.*;
import com.meetingos.event.ActionItemEventProducer;
import com.meetingos.event.DecisionEventProducer;
import com.meetingos.llm.LlmClient;
import com.meetingos.repository.ActionItemRepository;
import com.meetingos.repository.DecisionRepository;
import com.meetingos.repository.MeetingRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionService {

    private final MeetingRepository meetingRepository;
    private final DecisionRepository decisionRepository;
    private final ActionItemRepository actionItemRepository;
    private final ObjectMapper objectMapper;
    private final LlmClient llmClient;
    private final DecisionEventProducer decisionEventProducer;
    private final ActionItemEventProducer actionItemEventProducer;
    private final MetricsConfig metrics;

    @Transactional
    public ExtractionResponse extract(UUID meetingId) {
        metrics.getExtractionRequests().increment();
        return metrics.getExtractionLatency().record(() -> doExtract(meetingId));
    }

    private ExtractionResponse doExtract(UUID meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", meetingId));

        if (meeting.getRawTranscript() == null || meeting.getRawTranscript().isBlank()) {
            throw new ExtractionException("Meeting has no transcript to extract from");
        }

        String prompt = buildPrompt(meeting.getRawTranscript(), meeting.getDate().getYear());
        String rawJson = llmClient.call(prompt);
        ExtractionResult result = parseResponse(rawJson);

        // Save decisions
        Map<String, UUID> decisionTitleToId = new HashMap<>();
        List<Decision> savedDecisions = new ArrayList<>();
        for (ExtractionResult.DecisionData d : result.getDecisions()) {
            Decision decision = Decision.builder()
                    .meetingId(meetingId)
                    .title(d.getTitle())
                    .reason(d.getReasonAsString())
                    .alternatives(d.getAlternatives())
                    .tradeoffs(d.getTradeoffs())
                    .owner(d.getOwner())
                    .status(DecisionStatus.PROPOSED)
                    .build();
            Decision saved = decisionRepository.save(decision);
            savedDecisions.add(saved);
            decisionTitleToId.put(d.getTitle().toLowerCase(), saved.getId());
            decisionEventProducer.sendDecisionCreated(saved);
        }

        // Save action items, linking to decisions where possible
        List<ActionItem> savedActionItems = new ArrayList<>();
        for (ExtractionResult.ActionItemData a : result.getActionItems()) {
            UUID decisionId = null;
            if (a.getRelatedDecision() != null) {
                decisionId = decisionTitleToId.get(a.getRelatedDecision().toLowerCase());
            }

            LocalDate dueDate = parseDueDate(a.getDueDate(), meeting.getDate().getYear());

            ActionItem item = ActionItem.builder()
                    .meetingId(meetingId)
                    .decisionId(decisionId)
                    .task(a.getTask())
                    .assignedTo(a.getAssignedTo())
                    .dueDate(dueDate != null ? dueDate : LocalDate.now().plusWeeks(1))
                    .status(ActionItemStatus.PENDING)
                    .build();
            savedActionItems.add(actionItemRepository.save(item));
            actionItemEventProducer.sendActionItemCreated(savedActionItems.getLast());
        }

        return ExtractionResponse.builder()
                .meetingId(meetingId)
                .decisionsExtracted(savedDecisions.size())
                .actionItemsExtracted(savedActionItems.size())
                .decisions(savedDecisions.stream().map(d -> ExtractionResponse.DecisionSummary.builder()
                        .id(d.getId()).title(d.getTitle()).owner(d.getOwner()).build()).toList())
                .actionItems(savedActionItems.stream().map(a -> ExtractionResponse.ActionItemSummary.builder()
                        .id(a.getId()).task(a.getTask()).assignedTo(a.getAssignedTo())
                        .dueDate(a.getDueDate()).build()).toList())
                .build();
    }

    private String buildPrompt(String transcript, int meetingYear) {
        return """
                Analyze this meeting transcript and extract decisions and action items.
                
                IMPORTANT DISTINCTIONS:
                - A DECISION is a finalized choice between alternatives. It requires that options were considered and one was chosen.
                - An ACTION ITEM is a task assigned to someone. Do NOT classify action items as decisions.
                - If someone is told to "set up X" or "do Y by date", that is an action item, NOT a decision.
                - Decision titles must describe the actual choice made, not the topic. Good: "Use Redis for caching". Bad: "Caching Strategy".
                
                The meeting year is %d. Use this year for any dates that only mention month/day.
                
                Return ONLY valid JSON in exactly this format, no other text:
                {
                  "decisions": [
                    {
                      "title": "short decision title",
                      "reason": "why this was decided",
                      "alternatives": ["other options that were considered"],
                      "tradeoffs": ["tradeoffs discussed"],
                      "owner": "person who owns this decision"
                    }
                  ],
                  "action_items": [
                    {
                      "task": "what needs to be done",
                      "assigned_to": "person responsible",
                      "due_date": "YYYY-MM-DD or null if not specified",
                      "related_decision": "decision title this relates to, or null"
                    }
                  ]
                }
                
                Rules:
                - Only extract decisions where alternatives were explicitly discussed or implied
                - Extract EVERY action item, even if no deadline was given
                - due_date must be YYYY-MM-DD format. Use year %d if only month/day mentioned
                - Use null for due_date if no deadline mentioned at all
                - Use null for related_decision if the action item is standalone
                - owner/assigned_to should be a person's name from the transcript
                
                TRANSCRIPT:
                """.formatted(meetingYear, meetingYear) + transcript;
    }

    private ExtractionResult parseResponse(String rawJson) {
        try {
            // LLMs sometimes wrap JSON in markdown code blocks
            String cleaned = rawJson.strip();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.strip();

            ExtractionResult result = objectMapper.readValue(cleaned, ExtractionResult.class);
            if (result.getDecisions() == null) result.setDecisions(List.of());
            if (result.getActionItems() == null) result.setActionItems(List.of());
            return result;
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", rawJson);
            throw new ExtractionException("Failed to parse extraction response: " + e.getMessage());
        }
    }

    private LocalDate parseDueDate(String dateStr, int meetingYear) {
        if (dateStr == null || dateStr.isBlank() || "null".equalsIgnoreCase(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            // Try MM-DD format and prepend meeting year
            try {
                return LocalDate.parse(meetingYear + "-" + dateStr);
            } catch (Exception e2) {
                log.warn("Could not parse due date: {}", dateStr);
                return null;
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractionResult {
        private List<DecisionData> decisions;
        @JsonProperty("action_items")
        private List<ActionItemData> actionItems;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DecisionData {
            private String title;
            private Object reason;
            private List<String> alternatives;
            private List<String> tradeoffs;
            private String owner;

            public String getReasonAsString() {
                if (reason == null) return null;
                return reason instanceof String ? (String) reason : reason.toString();
            }
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ActionItemData {
            private String task;
            @JsonProperty("assigned_to")
            private String assignedTo;
            @JsonProperty("due_date")
            private String dueDate;
            @JsonProperty("related_decision")
            private String relatedDecision;
        }
    }
}
