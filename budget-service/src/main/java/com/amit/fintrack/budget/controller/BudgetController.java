package com.amit.fintrack.budget.controller;

import com.amit.fintrack.budget.dto.BudgetRequest;
import com.amit.fintrack.budget.dto.BudgetResponse;
import com.amit.fintrack.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget APIs", description = "Manage monthly category budgets and spending status")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "Create monthly category budget")
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @RequestHeader(AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse response = budgetService.createBudget(
                request,
                authorizationHeader
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get monthly budgets with spending status")
    @GetMapping("/monthly")
    public ResponseEntity<List<BudgetResponse>> getMonthlyBudgets(
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(AUTHORIZATION) String authorizationHeader
    ) {
        return ResponseEntity.ok(
                budgetService.getMonthlyBudgets(year, month, authorizationHeader)
        );
    }

    @Operation(summary = "Get budget by ID")
    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @PathVariable UUID budgetId,
            @RequestHeader(AUTHORIZATION) String authorizationHeader
    ) {
        return ResponseEntity.ok(
                budgetService.getBudgetById(budgetId, authorizationHeader)
        );
    }

    @Operation(summary = "Update budget")
    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable UUID budgetId,
            @RequestHeader(AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse response = budgetService.updateBudget(
                budgetId,
                request,
                authorizationHeader
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete budget")
    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable UUID budgetId
    ) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }
}