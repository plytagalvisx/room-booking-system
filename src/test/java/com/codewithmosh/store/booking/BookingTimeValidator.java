package com.codewithmosh.store.booking;

import java.time.LocalDateTime;

public class BookingTimeValidator {
    public void validate(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}

//BookingTimeValidatorTest
//        ↓
//plain Java + JUnit
//        ↓
//no Spring
//no PostgreSQL
//no Docker