package com.swiftpay.gateway.service;

import com.swiftpay.common.constants.KafkaTopics;
import com.swiftpay.common.event.PaymentInitiatedEvent;
import com.swiftpay.common.exception.AccountNotFoundException;
import com.swiftpay.common.exception.InsufficientFundsException;
import com.swiftpay.common.exception.TransactionNotFoundException;
import com.swiftpay.common.model.TransactionStatus;
import com.swiftpay.common.util.JsonUtil;
import com.swiftpay.gateway.dto.PaymentRequest;
import com.swiftpay.gateway.dto.PaymentResponse;
import com.swiftpay.gateway.entity.Account;
import com.swiftpay.gateway.entity.OutboxEvent;
import com.swiftpay.gateway.entity.Transaction;
import com.swiftpay.gateway.mapper.TransactionMapper;
import com.swiftpay.gateway.repository.AccountRepository;
import com.swiftpay.gateway.repository.OutboxEventRepository;
import com.swiftpay.gateway.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final IdempotencyService idempotencyService;
    private final BalanceCacheService balanceCacheService;
    private final TransactionMapper transactionMapper;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request, String idempotencyKey) {
        var cached = idempotencyService.getIfPresent(idempotencyKey);
        if (cached.isPresent()) return cached.get();

        if (request.getSenderId().equals(request.getReceiverId()))
            throw new IllegalArgumentException("Sender and receiver must be different accounts");

        Account sender = accountRepository.findActiveById(request.getSenderId())
                .orElseThrow(() -> new AccountNotFoundException(request.getSenderId()));
        Account receiver = accountRepository.findActiveById(request.getReceiverId())
                .orElseThrow(() -> new AccountNotFoundException(request.getReceiverId()));

        if (!sender.getCurrency().equals(request.getCurrency()))
            throw new IllegalArgumentException("Currency mismatch: sender uses " + sender.getCurrency());

        BigDecimal balance = balanceCacheService.getBalance(sender.getId()).orElse(sender.getBalance());
        if (balance.compareTo(request.getAmount()) < 0)
            throw new InsufficientFundsException(sender.getId(), balance, request.getAmount());

        Transaction tx = transactionRepository.save(Transaction.builder()
                .sender(sender).receiver(receiver)
                .amount(request.getAmount()).currency(request.getCurrency())
                .status(TransactionStatus.PENDING).idempotencyKey(idempotencyKey)
                .build());

        outboxEventRepository.save(OutboxEvent.builder()
                .aggregateId(tx.getId()).aggregateType("Transaction")
                .eventType(KafkaTopics.PAYMENT_INITIATED)
                .payload(JsonUtil.toJson(PaymentInitiatedEvent.of(
                        tx.getId(), sender.getId(), receiver.getId(),
                        request.getAmount(), request.getCurrency(), idempotencyKey)))
                .published(false).retryCount(0).build());

        balanceCacheService.storeBalance(sender.getId(), sender.getBalance());
        PaymentResponse response = transactionMapper.toPaymentResponse(tx);
        idempotencyService.store(idempotencyKey, response);

        log.info("Payment initiated: txId={} sender={} amount={}", tx.getId(), sender.getId(), request.getAmount());
        return response;
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getTransactionHistory(UUID userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable).map(transactionMapper::toPaymentResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .map(transactionMapper::toPaymentResponse)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }
}