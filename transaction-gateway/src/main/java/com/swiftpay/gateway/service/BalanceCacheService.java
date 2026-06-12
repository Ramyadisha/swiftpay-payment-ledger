package com.swiftpay.gateway.service;

import com.swiftpay.common.constants.CacheKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${swiftpay.balance-cache.ttl-minutes:5}")
    private int ttlMinutes;

    public Optional<BigDecimal> getBalance(UUID accountId) {
        try {
            Object value = redisTemplate.opsForValue().get(CacheKeys.balanceKey(accountId));
            if (value != null) return Optional.of(new BigDecimal(value.toString()));
        } catch (Exception e) {
            log.warn("Balance cache read failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void storeBalance(UUID accountId, BigDecimal balance) {
        try {
            redisTemplate.opsForValue().set(
                    CacheKeys.balanceKey(accountId), balance.toPlainString(), Duration.ofMinutes(ttlMinutes));
        } catch (Exception e) {
            log.warn("Balance cache write failed: {}", e.getMessage());
        }
    }

    public void evictBalance(UUID accountId) {
        try {
            redisTemplate.delete(CacheKeys.balanceKey(accountId));
        } catch (Exception e) {
            log.warn("Balance cache eviction failed: {}", e.getMessage());
        }
    }
}