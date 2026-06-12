package com.swiftpay.ledger.dto;

import com.swiftpay.common.model.TransactionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionDto {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String failureReason;
    private Instant updatedAt;
}