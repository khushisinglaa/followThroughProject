package com.meetingos.controller;

import com.meetingos.dto.CreateMeetingRequest;
import com.meetingos.dto.ExtractionResponse;
import com.meetingos.dto.MeetingResponse;
import com.meetingos.service.ExtractionService;
import com.meetingos.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final ExtractionService extractionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(@Valid @RequestBody CreateMeetingRequest request) {
        return meetingService.create(request);
    }

    @GetMapping("/{id}")
    public MeetingResponse getById(@PathVariable UUID id) {
        return meetingService.getById(id);
    }

    @GetMapping
    public Page<MeetingResponse> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return meetingService.getAll(pageable);
    }

    @PostMapping("/{id}/extract")
    public ExtractionResponse extract(@PathVariable UUID id) {
        return extractionService.extract(id);
    }
}
