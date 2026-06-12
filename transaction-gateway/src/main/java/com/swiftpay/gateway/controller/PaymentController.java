package com.swiftpay.gateway.controller;

import com.swiftpay.common.dto.ApiResponse;
import com.swiftpay.gateway.dto.PaymentRequest;
import com.swiftpay.gateway.dto.PaymentResponse;
import com.swiftpay.gateway.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
@SecurityRequirement(name = "BearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Initiate a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(paymentService.initiatePayment(request, idempotencyKey), "Payment accepted"));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getTransaction(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransaction(transactionId)));
    }

    @GetMapping("/users/{userId}/history")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getHistory(
            @PathVariable UUID userId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransactionHistory(userId, pageable)));
    }
}