package com.meetingos.scheduler;

import com.meetingos.config.MetricsConfig;
import com.meetingos.entity.ActionItem;
import com.meetingos.entity.ActionItemStatus;
import com.meetingos.event.ActionItemEventProducer;
import com.meetingos.repository.ActionItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueScheduler {

    private final ActionItemRepository actionItemRepository;
    private final ActionItemEventProducer eventProducer;
    private final MetricsConfig metrics;

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void markOverdueItems() {
        List<ActionItem> overdueItems = actionItemRepository.findOverdue(LocalDate.now());

        if (overdueItems.isEmpty()) {
            log.info("Overdue check: no overdue items found");
            return;
        }

        log.info("Overdue check: marking {} items as OVERDUE", overdueItems.size());
        metrics.getOverdueGauge().set(overdueItems.size());

        for (ActionItem item : overdueItems) {
            item.setStatus(ActionItemStatus.OVERDUE);
            actionItemRepository.save(item);
            eventProducer.sendActionItemOverdue(item);
        }
    }
}
