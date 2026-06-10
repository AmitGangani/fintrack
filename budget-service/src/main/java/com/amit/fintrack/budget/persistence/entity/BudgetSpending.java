package com.amit.fintrack.budget.persistence.entity;

import com.amit.fintrack.budget.domain.BudgetCategory;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "budget_spending",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_budget_spending_user_category_month_year",
                        columnNames = {"user_id", "category", "month", "year"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetSpending {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetCategory category;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(name = "spent_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal spentAmount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}