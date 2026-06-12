package com.swiftpay.ledger.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterConsumer {

    @KafkaListener(topics = "payment-initiated.DLT", groupId = "ledger-dlt-group")
    public void handleDlt(@Payload String payload,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("DLT message on topic={}: {}", topic, payload);
    }
}