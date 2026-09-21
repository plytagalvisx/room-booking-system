package com.codewithmosh.store.booking.dto;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        Long roomId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}

//A response might be:
//{
//    "id": 15,
//    "userId": 1,
//    "roomId": 3,
//    "startTime": "2026-09-10T10:00:00",
//    "endTime": "2026-09-10T11:00:00"
//}
//This also prevents JPA entities and their relationships from leaking directly into our JSON API.