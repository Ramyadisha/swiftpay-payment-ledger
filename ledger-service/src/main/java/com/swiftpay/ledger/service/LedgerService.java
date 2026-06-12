package com.swiftpay.ledger.service;

import com.swiftpay.common.constants.KafkaTopics;
import com.swiftpay.common.event.PaymentCompletedEvent;
import com.swiftpay.common.event.PaymentFailedEvent;
import com.swiftpay.common.event.PaymentInitiatedEvent;
import com.swiftpay.common.model.TransactionStatus;
import com.swiftpay.ledger.entity.Account;
import com.swiftpay.ledger.entity.AuditLog;
import com.swiftpay.ledger.entity.Transaction;
import com.swiftpay.ledger.repository.AccountRepository;
import com.swiftpay.ledger.repository.AuditLogRepository;
import com.swiftpay.ledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void processPayment(PaymentInitiatedEvent event) {
        log.info("Processing payment: txId={}", event.getTransactionId());

        Transaction tx = transactionRepository.findById(event.getTransactionId())
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found: " + event.getTransactionId()));

        if (tx.getStatus() != TransactionStatus.PENDING) {
            log.warn("Transaction {} already processed: {}", tx.getId(), tx.getStatus());
            return;
        }

        tx.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(tx);

        try {
            Account sender = accountRepository.findByIdWithLock(event.getSenderId())
                    .orElseThrow(() -> new RuntimeException("Sender not found: " + event.getSenderId()));
            Account receiver = accountRepository.findByIdWithLock(event.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found: " + event.getReceiverId()));

            if (sender.getBalance().compareTo(event.getAmount()) < 0) {
                throw new RuntimeException("Insufficient funds for account: " + sender.getId());
            }

            BigDecimal senderBefore   = sender.getBalance();
            BigDecimal receiverBefore = receiver.getBalance();

            sender.setBalance(senderBefore.subtract(event.getAmount()));
            receiver.setBalance(receiverBefore.add(event.getAmount()));

            accountRepository.save(sender);
            accountRepository.save(receiver);

            auditLogRepository.save(AuditLog.builder()
                    .transactionId(tx.getId()).accountId(sender.getId())
                    .operation("DEBIT").amountDelta(event.getAmount().negate())
                    .balanceBefore(senderBefore).balanceAfter(sender.getBalance())
                    .performedBy("ledger-service").build());

            auditLogRepository.save(AuditLog.builder()
                    .transactionId(tx.getId()).accountId(receiver.getId())
                    .operation("CREDIT").amountDelta(event.getAmount())
                    .balanceBefore(receiverBefore).balanceAfter(receiver.getBalance())
                    .performedBy("ledger-service").build());

            tx.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(tx);

            kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED,
                    tx.getId().toString(),
                    PaymentCompletedEvent.of(tx.getId(), sender.getId(),
                            receiver.getId(), event.getAmount(), event.getCurrency()));

            log.info("Payment COMPLETED: txId={} amount={}", tx.getId(), event.getAmount());

        } catch (Exception e) {
            log.error("Payment FAILED: txId={} reason={}", tx.getId(), e.getMessage());
            tx.setStatus(TransactionStatus.FAILED);
            tx.setFailureReason(e.getMessage());
            transactionRepository.save(tx);

            kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED,
                    tx.getId().toString(),
                    PaymentFailedEvent.of(tx.getId(), event.getSenderId(),
                            event.getReceiverId(), event.getAmount(),
                            event.getCurrency(), e.getMessage()));
        }
    }
}