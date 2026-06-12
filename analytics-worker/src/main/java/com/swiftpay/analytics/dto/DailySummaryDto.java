package com.swiftpay.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DailySummaryDto {
    private LocalDate date;
    private BigDecimal totalVolume;
    private long transactionCount;
}