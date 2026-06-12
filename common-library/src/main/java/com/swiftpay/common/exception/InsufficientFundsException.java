package com.swiftpay.common.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    private final UUID accountId;
    private final BigDecimal available;
    private final BigDecimal requested;

    public InsufficientFundsException(UUID accountId, BigDecimal available, BigDecimal requested) {
        super("Insufficient funds for account %s: available=%s, requested=%s"
                .formatted(accountId, available, requested));
        this.accountId = accountId;
        this.available = available;
        this.requested = requested;
    }

    public UUID getAccountId() { return accountId; }
    public BigDecimal getAvailable() { return available; }
    public BigDecimal getRequested() { return requested; }
}