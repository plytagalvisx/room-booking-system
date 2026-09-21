package com.codewithmosh.store.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record BookingRequest(
//        @NotNull
//        @Positive
//        Long userId,
        // Update: The browser is no longer allowed to decide who owns the booking.

        @NotNull
        @Positive
        Long roomId,

        @NotNull
        @Future
        LocalDateTime startTime,

        @NotNull
        @Future
        LocalDateTime endTime
) {
}


//The client therefore sends:
//{
//    "userId": 1,
//    "roomId": 3,
//    "startTime": "2026-09-10T10:00:00",
//    "endTime": "2026-09-10T11:00:00"
//}

//This is much better than accepting:
//{
//    "user": {
//        "id": 1,
//        "name": "...",
//        "email": "..."
//    },
//    "room": {
//        ...
//    }
//}

//The client only needs to say:
//- User 1 wants to reserve room 3.

//The backend retrieves those actual entities.