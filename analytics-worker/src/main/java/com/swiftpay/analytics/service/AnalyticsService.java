package com.swiftpay.analytics.service;

import com.swiftpay.analytics.dto.DailySummaryDto;
import com.swiftpay.analytics.dto.TopUserDto;
import com.swiftpay.analytics.entity.AnalyticsRecord;
import com.swiftpay.analytics.repository.AnalyticsRecordRepository;
import com.swiftpay.common.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRecordRepository repository;

    @Transactional
    public void recordCompletedPayment(PaymentCompletedEvent event) {
        if (repository.existsByTransactionId(event.getTransactionId())) {
            log.warn("Analytics already recorded for txId={}", event.getTransactionId());
            return;
        }
        repository.save(AnalyticsRecord.builder()
                .transactionId(event.getTransactionId())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .transactionDate(event.getOccurredAt()
                        .atZone(ZoneOffset.UTC).toLocalDate())
                .build());
        log.info("Analytics recorded: txId={}", event.getTransactionId());
    }

    @Transactional(readOnly = true)
    public DailySummaryDto getDailySummary(LocalDate date) {
        Object[] result = repository.getDailySummary(date);
        return DailySummaryDto.builder()
                .date(date)
                .totalVolume(result[0] != null ? (BigDecimal) result[0] : BigDecimal.ZERO)
                .transactionCount(result[1] != null ? ((Number) result[1]).longValue() : 0L)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TopUserDto> getTopSenders(LocalDate from, LocalDate to) {
        return repository.findTopSendersBetween(from, to).stream()
                .map(row -> TopUserDto.builder()
                        .userId((UUID) row[0])
                        .totalSent((BigDecimal) row[1])
                        .transactionCount(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalVolume(LocalDate from) {
        BigDecimal v = repository.getTotalVolumeFrom(from);
        return v != null ? v : BigDecimal.ZERO;
    }
}