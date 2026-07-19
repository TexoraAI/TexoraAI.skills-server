package com.lms.attendance.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public class DateRangeResolver {

    private DateRangeResolver() {}

    // Supported filterType values:
    // TODAY, YESTERDAY, LAST_7_DAYS, LAST_14_DAYS, LAST_30_DAYS, THIS_WEEK, THIS_MONTH, CUSTOM
    public static LocalDate[] resolve(String filterType, LocalDate customStart, LocalDate customEnd) {
        if (filterType == null || filterType.isBlank()) {
            throw new IllegalArgumentException("filterType is required");
        }

        LocalDate today = LocalDate.now();

        switch (filterType.trim().toUpperCase()) {
            case "TODAY":
                return new LocalDate[]{today, today};

            case "YESTERDAY":
                LocalDate yesterday = today.minusDays(1);
                return new LocalDate[]{yesterday, yesterday};

            case "LAST_7_DAYS":
                return new LocalDate[]{today.minusDays(6), today};

            case "LAST_14_DAYS":
                return new LocalDate[]{today.minusDays(13), today};

            case "LAST_30_DAYS":
                return new LocalDate[]{today.minusDays(29), today};

            case "THIS_WEEK":
                return new LocalDate[]{today.with(DayOfWeek.MONDAY), today};

            case "THIS_MONTH":
                return new LocalDate[]{YearMonth.from(today).atDay(1), today};

            case "CUSTOM":
                if (customStart == null || customEnd == null) {
                    throw new IllegalArgumentException("startDate and endDate are required for CUSTOM filterType");
                }
                if (customStart.isAfter(customEnd)) {
                    throw new IllegalArgumentException("startDate must not be after endDate");
                }
                return new LocalDate[]{customStart, customEnd};

            default:
                throw new IllegalArgumentException("Unknown filterType: " + filterType);
        }
    }
}