package com.amit.fintrack.analytics.controller;

import com.amit.fintrack.analytics.dto.CategoryExpenseAnalyticsResponse;
import com.amit.fintrack.analytics.dto.MonthlyAnalyticsResponse;
import com.amit.fintrack.analytics.service.AnalyticsQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics APIs", description = "Monthly income, expense, savings, and category analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    @Operation(summary = "Get monthly income, expense, and net savings")
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAnalyticsResponse> getMonthlyAnalytics(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                analyticsQueryService.getMonthlyAnalytics(year, month)
        );
    }

    @Operation(summary = "Get category-wise monthly expenses")
    @GetMapping("/category-expenses")
    public ResponseEntity<List<CategoryExpenseAnalyticsResponse>> getCategoryExpenses(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                analyticsQueryService.getCategoryExpenses(year, month)
        );
    }
}