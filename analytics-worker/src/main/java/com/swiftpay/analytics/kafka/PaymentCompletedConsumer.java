package com.swiftpay.analytics.kafka;

import com.swiftpay.analytics.service.AnalyticsService;
import com.swiftpay.common.event.PaymentCompletedEvent;
import com.swiftpay.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedConsumer {

    private final AnalyticsService analyticsService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(
            topics = "payment-completed",
            groupId = "analytics-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload String payload,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Analytics received from topic={} offset={}", topic, offset);
        try {
            PaymentCompletedEvent event = JsonUtil.fromJson(payload, PaymentCompletedEvent.class);
            analyticsService.recordCompletedPayment(event);
        } catch (Exception e) {
            log.error("Analytics processing failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}