package com.swiftpay.analytics.controller;

import com.swiftpay.analytics.dto.DailySummaryDto;
import com.swiftpay.analytics.dto.TopUserDto;
import com.swiftpay.analytics.service.AnalyticsService;
import com.swiftpay.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/daily-summary")
    @Operation(summary = "Get daily transaction summary")
    public ResponseEntity<ApiResponse<DailySummaryDto>> getDailySummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDailySummary(date)));
    }

    @GetMapping("/top-senders")
    @Operation(summary = "Get top senders in date range")
    public ResponseEntity<ApiResponse<List<TopUserDto>>> getTopSenders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTopSenders(from, to)));
    }

    @GetMapping("/total-volume")
    @Operation(summary = "Get total payment volume since a date")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTotalVolume(from)));
    }
}