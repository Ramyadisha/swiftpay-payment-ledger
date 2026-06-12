package com.swiftpay.ledger.controller;

import com.swiftpay.common.dto.ApiResponse;
import com.swiftpay.ledger.dto.TransactionDto;
import com.swiftpay.ledger.mapper.TransactionMapper;
import com.swiftpay.ledger.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger")
public class LedgerController {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @GetMapping("/transactions/{userId}")
    @Operation(summary = "Get transaction history for a user")
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getHistory(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionRepository.findByUserId(userId, pageable).map(transactionMapper::toDto)));
    }

    @GetMapping("/transactions/detail/{transactionId}")
    @Operation(summary = "Get transaction details")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransaction(@PathVariable UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .map(t -> ResponseEntity.ok(ApiResponse.success(transactionMapper.toDto(t))))
                .orElse(ResponseEntity.notFound().build());
    }
}