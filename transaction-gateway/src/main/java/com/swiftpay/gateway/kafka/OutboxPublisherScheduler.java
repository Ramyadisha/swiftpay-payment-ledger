package com.swiftpay.gateway.kafka;

import com.swiftpay.gateway.entity.OutboxEvent;
import com.swiftpay.gateway.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${swiftpay.outbox.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${swiftpay.outbox.scheduler-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findUnpublishedEvents(batchSize);
        if (events.isEmpty()) return;
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId().toString(), event.getPayload())
                        .whenComplete((r, ex) -> {
                            if (ex == null) {
                                event.setPublished(true);
                                event.setPublishedAt(Instant.now());
                            } else {
                                event.setRetryCount(event.getRetryCount() + 1);
                                log.warn("Outbox publish failed for {}: {}", event.getId(), ex.getMessage());
                            }
                            outboxEventRepository.save(event);
                        });
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                outboxEventRepository.save(event);
            }
        }
    }
}