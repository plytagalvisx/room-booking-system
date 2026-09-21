package com.codewithmosh.store.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

//    boolean existsByRoom_IdAndStartTimeLessThanAndEndTimeGreaterThan(
//            Long roomId, LocalDateTime requestedEnd, LocalDateTime requestedStart
//    ); // This method corresponds roughly to:
    //    SELECT *
    //    FROM bookings
    //    WHERE room_id = ?
    //    AND start_time < requested_end
    //    AND end_time > requested_start;

    // OBS! That's how we detect an overlapping booking.
    // If the later booking overlaps, then it should be rejected.

    // Alternative:
    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.room.id = :roomId
          AND b.startTime < :endTime
          AND b.endTime > :startTime
    """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Booking> findByUser_IdOrderByStartTimeAsc(Long userId);
    // Spring Data interprets that as roughly:
    //    SELECT *
    //    FROM bookings
    //    WHERE user_id = ?
    //    ORDER BY start_time ASC
}
