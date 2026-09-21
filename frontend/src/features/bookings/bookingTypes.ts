export type BookingRequest = {
    // userId: number;
    roomId: number;
    startTime: string;
    endTime: string;
};

export type BookingResponse = {
    id: number;
    userId: number;
    roomId: number;
    startTime: string;
    endTime: string;
};

// This mirrors our Java DTO conceptually:

// BookingRequest(
//     Long userId,
//     Long roomId,
//     LocalDateTime startTime,
//     LocalDateTime endTime
// )

// For now, the browser's datetime-local inputs produce strings such as:
//
// 2030-01-10T10:00
//
// which Spring can deserialize into LocalDateTime.


