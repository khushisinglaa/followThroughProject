package com.meetingos.controller;

import com.meetingos.dto.ActionItemResponse;
import com.meetingos.dto.CreateActionItemRequest;
import com.meetingos.dto.UpdateStatusRequest;
import com.meetingos.service.ActionItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/action-items")
@RequiredArgsConstructor
public class ActionItemController {

    private final ActionItemService actionItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActionItemResponse create(@Valid @RequestBody CreateActionItemRequest request) {
        return actionItemService.create(request);
    }

    @GetMapping
    public List<ActionItemResponse> getAll() {
        return actionItemService.getAll();
    }

    @GetMapping("/overdue")
    public List<ActionItemResponse> getOverdue() {
        return actionItemService.getOverdue();
    }

    @PatchMapping("/{id}/status")
    public ActionItemResponse updateStatus(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateStatusRequest request) {
        return actionItemService.updateStatus(id, request.getStatus());
    }
}
