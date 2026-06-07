package com.meetingos.service;

import com.meetingos.dto.CreateDecisionRequest;
import com.meetingos.dto.DecisionResponse;
import com.meetingos.entity.Decision;
import com.meetingos.event.DecisionEventProducer;
import com.meetingos.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final DecisionEventProducer eventProducer;

    @Transactional
    @CacheEvict(value = "decisions", allEntries = true)
    public DecisionResponse create(CreateDecisionRequest request) {
        Decision decision = Decision.builder()
                .meetingId(request.getMeetingId())
                .title(request.getTitle())
                .reason(request.getReason())
                .alternatives(request.getAlternatives())
                .tradeoffs(request.getTradeoffs())
                .owner(request.getOwner())
                .status(request.getStatus())
                .build();
        Decision saved = decisionRepository.save(decision);
        eventProducer.sendDecisionCreated(saved);
        log.info("Cache evicted: decisions");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DecisionResponse getById(UUID id) {
        return decisionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "decisions")
    public List<DecisionResponse> getAll() {
        log.info("Cache MISS: loading all decisions from DB");
        return decisionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> search(String query) {
        return decisionRepository.fullTextSearch(query).stream().map(this::toResponse).toList();
    }

    private DecisionResponse toResponse(Decision d) {
        return DecisionResponse.builder()
                .id(d.getId())
                .meetingId(d.getMeetingId())
                .title(d.getTitle())
                .reason(d.getReason())
                .alternatives(d.getAlternatives())
                .tradeoffs(d.getTradeoffs())
                .owner(d.getOwner())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
