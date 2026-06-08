package com.amit.fintrack.analytics.controller;

import com.amit.fintrack.analytics.dto.CategoryExpenseAnalyticsResponse;
import com.amit.fintrack.analytics.dto.MonthlyAnalyticsResponse;
import com.amit.fintrack.analytics.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAnalyticsResponse> getMonthlyAnalytics(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                analyticsQueryService.getMonthlyAnalytics(year, month)
        );
    }

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