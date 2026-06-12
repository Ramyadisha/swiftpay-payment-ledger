package com.swiftpay.gateway.service;

import com.swiftpay.common.constants.CacheKeys;
import com.swiftpay.gateway.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${swiftpay.idempotency.ttl-hours:24}")
    private int ttlHours;

    public Optional<PaymentResponse> getIfPresent(String idempotencyKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(CacheKeys.idempotencyKey(idempotencyKey));
            if (cached instanceof PaymentResponse r) return Optional.of(r);
        } catch (Exception e) {
            log.warn("Redis idempotency check failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void store(String idempotencyKey, PaymentResponse response) {
        try {
            redisTemplate.opsForValue().set(
                    CacheKeys.idempotencyKey(idempotencyKey), response, Duration.ofHours(ttlHours));
        } catch (Exception e) {
            log.warn("Failed to store idempotency entry: {}", e.getMessage());
        }
    }
}