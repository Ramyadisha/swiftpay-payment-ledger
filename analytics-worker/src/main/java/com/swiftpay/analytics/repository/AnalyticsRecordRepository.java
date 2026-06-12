package com.swiftpay.analytics.repository;

import com.swiftpay.analytics.entity.AnalyticsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalyticsRecordRepository extends JpaRepository<AnalyticsRecord, UUID> {

    Optional<AnalyticsRecord> findByTransactionId(UUID transactionId);

    boolean existsByTransactionId(UUID transactionId);

    @Query("""
            SELECT a.senderId, SUM(a.amount), COUNT(a)
            FROM AnalyticsRecord a
            WHERE a.transactionDate >= :from AND a.transactionDate <= :to
            GROUP BY a.senderId
            ORDER BY SUM(a.amount) DESC
            LIMIT 10
            """)
    List<Object[]> findTopSendersBetween(@Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    @Query("SELECT SUM(a.amount), COUNT(a) FROM AnalyticsRecord a WHERE a.transactionDate = :date")
    Object[] getDailySummary(@Param("date") LocalDate date);

    @Query("SELECT SUM(a.amount) FROM AnalyticsRecord a WHERE a.transactionDate >= :from")
    BigDecimal getTotalVolumeFrom(@Param("from") LocalDate from);
}