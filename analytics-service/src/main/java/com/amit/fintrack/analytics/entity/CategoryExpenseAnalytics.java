package com.amit.fintrack.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "category_expense_analytics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_expense_user_category_year_month",
                        columnNames = {"user_id", "category", "year", "month"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryExpenseAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(name = "total_expense", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalExpense;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}