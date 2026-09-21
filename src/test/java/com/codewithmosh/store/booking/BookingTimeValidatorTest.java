package com.codewithmosh.store.booking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

class BookingTimeValidatorTest {

    private final BookingTimeValidator validator = new BookingTimeValidator();

    @Test
    void shouldAcceptValidTimeRange() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

        assertDoesNotThrow(() -> validator.validate(start, end));
    }

    @Test
    void shouldRejectEndBeforeStart() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 11, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 10, 0);

        assertThrows(IllegalArgumentException.class, () -> validator.validate(start, end));
    }
}
