package com.swiftpay.ledger.repository;

import com.swiftpay.ledger.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByTransactionIdOrderByCreatedAtAsc(UUID transactionId);
}
