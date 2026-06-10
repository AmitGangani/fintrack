package com.amit.fintrack.budget.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Component
public class BudgetAlertPolicy {

    private static final BigDecimal WARNING_THRESHOLD = BigDecimal.valueOf(80);
    private static final BigDecimal EXCEEDED_THRESHOLD = BigDecimal.valueOf(100);

    public Optional<BudgetAlertDecision> evaluate(
            BudgetCategory category,
            BigDecimal spentAmount,
            BigDecimal limitAmount
    ) {
        BigDecimal percentageUsed = calculatePercentageUsed(spentAmount, limitAmount);
        BudgetAlertType alertType = resolveAlertType(percentageUsed);

        if (alertType == null) {
            return Optional.empty();
        }

        return Optional.of(new BudgetAlertDecision(
                alertType,
                percentageUsed,
                buildMessage(category, spentAmount, limitAmount, percentageUsed, alertType)
        ));
    }

    public BigDecimal calculatePercentageUsed(BigDecimal spentAmount, BigDecimal limitAmount) {
        return spentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(limitAmount, 2, RoundingMode.HALF_UP);
    }

    public BudgetStatus calculateStatus(BigDecimal percentageUsed) {
        if (percentageUsed.compareTo(EXCEEDED_THRESHOLD) >= 0) {
            return BudgetStatus.EXCEEDED;
        }

        if (percentageUsed.compareTo(WARNING_THRESHOLD) >= 0) {
            return BudgetStatus.WARNING;
        }

        return BudgetStatus.SAFE;
    }

    private BudgetAlertType resolveAlertType(BigDecimal percentageUsed) {
        if (percentageUsed.compareTo(EXCEEDED_THRESHOLD) >= 0) {
            return BudgetAlertType.BUDGET_EXCEEDED;
        }

        if (percentageUsed.compareTo(WARNING_THRESHOLD) >= 0) {
            return BudgetAlertType.BUDGET_WARNING;
        }

        return null;
    }

    private String buildMessage(
            BudgetCategory category,
            BigDecimal spentAmount,
            BigDecimal limitAmount,
            BigDecimal percentageUsed,
            BudgetAlertType alertType
    ) {
        if (alertType == BudgetAlertType.BUDGET_EXCEEDED) {
            BigDecimal exceededBy = spentAmount.subtract(limitAmount);

            return "You exceeded your "
                    + category
                    + " budget by ₹"
                    + exceededBy
                    + ". Spent ₹"
                    + spentAmount
                    + " out of ₹"
                    + limitAmount
                    + ".";
        }

        return "You have used "
                + percentageUsed
                + "% of your "
                + category
                + " budget. Spent ₹"
                + spentAmount
                + " out of ₹"
                + limitAmount
                + ".";
    }
}
