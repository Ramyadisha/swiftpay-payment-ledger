package com.swiftpay.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private UUID eventId;
    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
    private String currency;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant occurredAt;

    public static PaymentCompletedEvent of(UUID transactionId, UUID senderId,
                                           UUID receiverId, BigDecimal amount,
                                           String currency) {
        return PaymentCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(transactionId)
                .senderId(senderId)
                .receiverId(receiverId)
                .amount(amount)
                .currency(currency)
                .occurredAt(Instant.now())
                .build();
    }
}