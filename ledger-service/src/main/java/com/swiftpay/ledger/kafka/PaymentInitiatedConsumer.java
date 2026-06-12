package com.swiftpay.ledger.kafka;

import com.swiftpay.common.event.PaymentInitiatedEvent;
import com.swiftpay.common.util.JsonUtil;
import com.swiftpay.ledger.service.LedgerService;
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
public class PaymentInitiatedConsumer {

    private final LedgerService ledgerService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(
            topics = "${swiftpay.kafka.topics.payment-initiated:payment-initiated}",
            groupId = "${spring.kafka.consumer.group-id:ledger-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload String payload,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received event from topic={} partition={} offset={}", topic, partition, offset);
        try {
            PaymentInitiatedEvent event = JsonUtil.fromJson(payload, PaymentInitiatedEvent.class);
            ledgerService.processPayment(event);
        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
            throw e;
        }
    }
}