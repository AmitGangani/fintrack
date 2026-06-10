package com.amit.fintrack.transaction.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TransactionSummaryCalculator {

    public MonthlySummary monthlySummary(int year, int month, List<TransactionSummaryItem> transactions) {
        BigDecimal totalIncome = sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(transactions, TransactionType.EXPENSE);

        return new MonthlySummary(
                year,
                month,
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                transactions.size()
        );
    }

    public List<CategoryExpenseSummary> categoryExpenseSummary(List<TransactionSummaryItem> transactions) {
        Map<TransactionCategory, BigDecimal> categoryTotals = transactions.stream()
                .filter(transaction -> transaction.type() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        TransactionSummaryItem::category,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                TransactionSummaryItem::amount,
                                BigDecimal::add
                        )
                ));

        return categoryTotals.entrySet()
                .stream()
                .map(entry -> new CategoryExpenseSummary(entry.getKey(), entry.getValue()))
                .toList();
    }

    private BigDecimal sumByType(List<TransactionSummaryItem> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.type() == type)
                .map(TransactionSummaryItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
