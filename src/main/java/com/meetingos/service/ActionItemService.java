package com.meetingos.service;

import com.meetingos.dto.ActionItemResponse;
import com.meetingos.dto.CreateActionItemRequest;
import com.meetingos.entity.ActionItem;
import com.meetingos.entity.ActionItemStatus;
import com.meetingos.event.ActionItemEventProducer;
import com.meetingos.repository.ActionItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;
    private final ActionItemEventProducer eventProducer;

    @Transactional
    @CacheEvict(value = "overdueItems", allEntries = true)
    public ActionItemResponse create(CreateActionItemRequest request) {
        ActionItem item = ActionItem.builder()
                .meetingId(request.getMeetingId())
                .decisionId(request.getDecisionId())
                .task(request.getTask())
                .assignedTo(request.getAssignedTo())
                .dueDate(request.getDueDate())
                .status(ActionItemStatus.PENDING)
                .build();
        ActionItem saved = actionItemRepository.save(item);
        eventProducer.sendActionItemCreated(saved);
        log.info("Cache evicted: overdueItems");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ActionItemResponse> getAll() {
        return actionItemRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "overdueItems")
    public List<ActionItemResponse> getOverdue() {
        log.info("Cache MISS: loading overdue items from DB");
        return actionItemRepository.findOverdue(LocalDate.now()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = "overdueItems", allEntries = true)
    public ActionItemResponse updateStatus(UUID id, ActionItemStatus status) {
        ActionItem item = actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActionItem", id));
        item.setStatus(status);
        ActionItem saved = actionItemRepository.saveAndFlush(item);

        if (status == ActionItemStatus.COMPLETED) {
            eventProducer.sendActionItemCompleted(saved);
        } else if (status == ActionItemStatus.OVERDUE) {
            eventProducer.sendActionItemOverdue(saved);
        }

        log.info("Cache evicted: overdueItems");
        return toResponse(saved);
    }

    private ActionItemResponse toResponse(ActionItem a) {
        return ActionItemResponse.builder()
                .id(a.getId())
                .meetingId(a.getMeetingId())
                .decisionId(a.getDecisionId())
                .task(a.getTask())
                .assignedTo(a.getAssignedTo())
                .dueDate(a.getDueDate())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
