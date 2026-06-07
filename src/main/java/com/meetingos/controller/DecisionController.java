package com.meetingos.controller;

import com.meetingos.dto.CreateDecisionRequest;
import com.meetingos.dto.DecisionResponse;
import com.meetingos.service.DecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public List<DecisionResponse> getAll() {
        return decisionService.getAll();
    }

    @GetMapping("/search")
    public List<DecisionResponse> search(@RequestParam String q) {
        return decisionService.search(q);
    }
}
