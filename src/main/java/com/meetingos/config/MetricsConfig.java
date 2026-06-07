package com.meetingos.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Getter
public class MetricsConfig {

    private final Counter extractionRequests;
    private final Timer extractionLatency;
    private final AtomicInteger overdueGauge;
    private final MeterRegistry registry;

    public MetricsConfig(MeterRegistry registry) {
        this.registry = registry;

        this.extractionRequests = Counter.builder("extraction_requests_total")
                .description("Total extraction requests")
                .register(registry);

        this.extractionLatency = Timer.builder("extraction_latency_seconds")
                .description("Extraction request latency")
                .register(registry);

        this.overdueGauge = registry.gauge("action_items_overdue_total",
                new AtomicInteger(0));
    }

    public Counter kafkaCounter(String eventType) {
        return Counter.builder("kafka_events_published_total")
                .tag("event_type", eventType)
                .register(registry);
    }
}
