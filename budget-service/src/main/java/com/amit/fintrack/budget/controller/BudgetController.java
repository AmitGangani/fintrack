package com.amit.fintrack.budget.controller;

import com.amit.fintrack.budget.dto.BudgetRequest;
import com.amit.fintrack.budget.dto.BudgetResponse;
import com.amit.fintrack.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse response = budgetService.createBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<BudgetResponse>> getMonthlyBudgets(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                budgetService.getMonthlyBudgets(year, month)
        );
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @PathVariable UUID budgetId
    ) {
        return ResponseEntity.ok(
                budgetService.getBudgetById(budgetId)
        );
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable UUID budgetId,
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse response = budgetService.updateBudget(
                budgetId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable UUID budgetId
    ) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }
}