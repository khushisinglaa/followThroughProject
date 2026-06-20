package com.meetingos.service;

import com.meetingos.dto.CreateMeetingRequest;
import com.meetingos.dto.MeetingResponse;
import com.meetingos.entity.Meeting;
import com.meetingos.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    @Transactional
    public MeetingResponse create(CreateMeetingRequest request) {
        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .date(request.getDate())
                .participants(request.getParticipants())
                .rawTranscript(request.getRawTranscript())
                .build();
        return toResponse(meetingRepository.save(meeting));
    }

    @Transactional(readOnly = true)
    public MeetingResponse getById(UUID id) {
        return meetingRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", id));
    }

    @Transactional(readOnly = true)
    public Page<MeetingResponse> getAll(Pageable pageable) {
        return meetingRepository.findAll(pageable).map(this::toResponse);
    }

    private MeetingResponse toResponse(Meeting m) {
        return MeetingResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .date(m.getDate())
                .participants(m.getParticipants())
                .rawTranscript(m.getRawTranscript())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
