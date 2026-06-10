package com.amit.fintrack.transaction.domain;

import java.time.LocalDate;

public record DateRange(LocalDate startDate, LocalDate endDate) {

    public static DateRange ofMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        return new DateRange(startDate, startDate.withDayOfMonth(startDate.lengthOfMonth()));
    }
}
