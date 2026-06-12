package com.swiftpay.common.constants;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String PAYMENT_INITIATED     = "payment-initiated";
    public static final String PAYMENT_COMPLETED     = "payment-completed";
    public static final String PAYMENT_FAILED        = "payment-failed";
    public static final String PAYMENT_INITIATED_DLT = "payment-initiated.DLT";
    public static final String PAYMENT_COMPLETED_DLT = "payment-completed.DLT";
    public static final String PAYMENT_FAILED_DLT    = "payment-failed.DLT";
}