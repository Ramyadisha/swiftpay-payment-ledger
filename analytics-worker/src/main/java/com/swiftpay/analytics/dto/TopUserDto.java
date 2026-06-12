package com.swiftpay.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TopUserDto {
    private UUID userId;
    private BigDecimal totalSent;
    private long transactionCount;
}