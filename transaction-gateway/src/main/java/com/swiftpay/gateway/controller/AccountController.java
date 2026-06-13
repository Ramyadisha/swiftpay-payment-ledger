package com.swiftpay.gateway.controller;

import com.swiftpay.common.dto.ApiResponse;
import com.swiftpay.gateway.dto.AccountDto;
import com.swiftpay.gateway.mapper.AccountMapper;
import com.swiftpay.gateway.repository.AccountRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts")
@SecurityRequirement(name = "BearerAuth")
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccount(@PathVariable UUID accountId) {
        return accountRepository.findById(accountId)
                .map(a -> ResponseEntity.ok(ApiResponse.success(accountMapper.toDto(a))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> listAccounts() {
        return ResponseEntity.ok(ApiResponse.success(
                accountRepository.findAll().stream().map(accountMapper::toDto).toList()));
    }

    @GetMapping("/hash")
    public String getHash() {
        return new BCryptPasswordEncoder(12).encode("Test@1234");
    }
}