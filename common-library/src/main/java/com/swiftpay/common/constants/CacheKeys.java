package com.swiftpay.common.constants;

import java.util.UUID;

public final class CacheKeys {
    private CacheKeys() {}

    public static final String IDEMPOTENCY_PREFIX = "idempotency:";
    public static final String BALANCE_PREFIX     = "balance:";

    public static String idempotencyKey(String key) {
        return IDEMPOTENCY_PREFIX + key;
    }

    public static String balanceKey(UUID accountId) {
        return BALANCE_PREFIX + accountId;
    }
}