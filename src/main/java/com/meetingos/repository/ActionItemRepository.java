package com.meetingos.repository;

import com.meetingos.entity.ActionItem;
import com.meetingos.entity.ActionItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ActionItemRepository extends JpaRepository<ActionItem, UUID> {

    List<ActionItem> findByMeetingId(UUID meetingId);

    @Query("SELECT a FROM ActionItem a WHERE a.status IN ('PENDING', 'IN_PROGRESS') AND a.dueDate < :today")
    List<ActionItem> findOverdue(LocalDate today);

    List<ActionItem> findByStatus(ActionItemStatus status);
}
