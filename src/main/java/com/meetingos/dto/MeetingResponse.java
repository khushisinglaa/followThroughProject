package com.meetingos.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingResponse {
    private UUID id;
    private String title;
    private LocalDate date;
    private List<String> participants;
    private String rawTranscript;
    private LocalDateTime createdAt;
}
