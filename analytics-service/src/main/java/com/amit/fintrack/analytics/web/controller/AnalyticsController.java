package com.amit.fintrack.analytics.web.controller;

import com.amit.fintrack.analytics.application.AnalyticsQueryService;
import com.amit.fintrack.analytics.application.model.CategoryExpenseAnalyticsView;
import com.amit.fintrack.analytics.application.model.MonthlyAnalyticsView;
import com.amit.fintrack.analytics.web.dto.CategoryExpenseAnalyticsResponse;
import com.amit.fintrack.analytics.web.dto.MonthlyAnalyticsResponse;
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
        return ResponseEntity.ok(toResponse(analyticsQueryService.getMonthlyAnalytics(year, month)));
    }

    @GetMapping("/category-expenses")
    public ResponseEntity<List<CategoryExpenseAnalyticsResponse>> getCategoryExpenses(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                analyticsQueryService.getCategoryExpenses(year, month).stream().map(this::toResponse).toList()
        );
    }

    private MonthlyAnalyticsResponse toResponse(MonthlyAnalyticsView analytics) {
        return new MonthlyAnalyticsResponse(
                analytics.year(),
                analytics.month(),
                analytics.totalIncome(),
                analytics.totalExpense(),
                analytics.netSavings()
        );
    }

    private CategoryExpenseAnalyticsResponse toResponse(CategoryExpenseAnalyticsView analytics) {
        return new CategoryExpenseAnalyticsResponse(analytics.category(), analytics.totalExpense());
    }
}
