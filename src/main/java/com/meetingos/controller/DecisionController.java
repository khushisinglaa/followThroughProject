package com.meetingos.controller;

import com.meetingos.dto.CreateDecisionRequest;
import com.meetingos.dto.DecisionResponse;
import com.meetingos.service.DecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DecisionResponse create(@Valid @RequestBody CreateDecisionRequest request) {
        return decisionService.create(request);
    }

    @GetMapping("/{id}")
    public DecisionResponse getById(@PathVariable UUID id) {
        return decisionService.getById(id);
    }

    @GetMapping
    public Page<DecisionResponse> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return decisionService.getAll(pageable);
    }

    @GetMapping("/meeting/{meetingId}")
    public List<DecisionResponse> getByMeetingId(@PathVariable UUID meetingId) {
        return decisionService.getByMeetingId(meetingId);
    }

    @GetMapping("/search")
    public List<DecisionResponse> search(@RequestParam String q) {
        return decisionService.search(q);
    }
}
