package com.amit.fintrack.budget.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "budget_alert_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_budget_alert_history_user_category_month_year_type",
                        columnNames = {"user_id", "category", "month", "year", "alert_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlertHistory {

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

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}