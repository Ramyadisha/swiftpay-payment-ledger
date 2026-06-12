package com.swiftpay.gateway.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountDto {
    private UUID id;
    private String username;
    private String email;
    private BigDecimal balance;
    private String currency;
    private String role;
    private Instant createdAt;
}