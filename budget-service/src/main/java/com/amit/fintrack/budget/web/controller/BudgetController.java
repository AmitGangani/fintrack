package com.amit.fintrack.budget.web.controller;

import com.amit.fintrack.budget.application.BudgetService;
import com.amit.fintrack.budget.application.model.BudgetCommand;
import com.amit.fintrack.budget.application.model.BudgetView;
import com.amit.fintrack.budget.web.dto.BudgetRequest;
import com.amit.fintrack.budget.web.dto.BudgetResponse;
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
        BudgetResponse response = toResponse(budgetService.createBudget(toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<BudgetResponse>> getMonthlyBudgets(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(budgetService.getMonthlyBudgets(year, month).stream().map(this::toResponse).toList());
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @PathVariable UUID budgetId
    ) {
        return ResponseEntity.ok(toResponse(budgetService.getBudgetById(budgetId)));
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable UUID budgetId,
            @Valid @RequestBody BudgetRequest request
    ) {
        return ResponseEntity.ok(toResponse(budgetService.updateBudget(budgetId, toCommand(request))));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable UUID budgetId
    ) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }

    private BudgetCommand toCommand(BudgetRequest request) {
        return new BudgetCommand(request.category(), request.month(), request.year(), request.limitAmount());
    }

    private BudgetResponse toResponse(BudgetView budget) {
        return new BudgetResponse(
                budget.id(),
                budget.category(),
                budget.month(),
                budget.year(),
                budget.limitAmount(),
                budget.spentAmount(),
                budget.remainingAmount(),
                budget.percentageUsed(),
                budget.status(),
                budget.createdAt(),
                budget.updatedAt()
        );
    }
}
