package com.codewithmosh.store.booking;

import com.codewithmosh.store.booking.dto.BookingRequest;
import com.codewithmosh.store.common.exception.BookingConflictException;
import com.codewithmosh.store.room.Room;
import com.codewithmosh.store.room.RoomRepository;
import com.codewithmosh.store.user.User;
import com.codewithmosh.store.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Concurrency test:
// We want to prove that your pessimistic locking actually prevents this:

// This test proves with two concurrent threads and real PostgreSQL that our pessimistic locking strategy prevents double booking.

// This is an integration test.

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BookingConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

//    private Long userId;
    private Long roomId;

    @BeforeEach
    void setup() {

        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setKeycloakSubject("test-keycloak-subject");
        userRepository.saveAndFlush(user);

        Room room = new Room();
        room.setName("Concurrency Room");
        room.setCapacity(10);
        room = roomRepository.saveAndFlush(room);
        roomId = room.getId();
    }

    @Test
    void onlyOneOfTwoConcurrentBookingsShouldSucceed() throws Exception {

        LocalDateTime start = LocalDateTime.of(2030, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 10, 11, 0);

        BookingRequest request1 = new BookingRequest(roomId, start, end);
        BookingRequest request2 = new BookingRequest(roomId, start, end);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        Future<Boolean> result1 =
                executor.submit(() -> {
                    ready.countDown();

                    startSignal.await();

                    try {
                        bookingService.createBooking(request1, "test-keycloak-subject", "testuser", "test@example.com");
                        return true;
                    } catch (BookingConflictException e) {
                        return false;
                    }
                });

        Future<Boolean> result2 =
                executor.submit(() -> {
                    ready.countDown();

                    startSignal.await();

                    try {
                        bookingService.createBooking(request2, "test-keycloak-subject", "testuser", "test@example.com");
                        return true;
                    } catch (BookingConflictException e) {
                        return false;
                    }
                });

        // Wait until both threads are ready.
        ready.await();

        // Release both at almost exactly the same time.
        startSignal.countDown();

        boolean firstSucceeded = result1.get();
        boolean secondSucceeded = result2.get();

        executor.shutdown();

        long successfulBookings = (firstSucceeded ? 1 : 0) + (secondSucceeded ? 1 : 0);

        assertEquals(1, successfulBookings);
        assertEquals(1, bookingRepository.count());
    }
}

// Now both threads resolve the same existing user:
//
//    Thread A                         Thread B
//       │                                │
//    subject=test-keycloak-subject       subject=test-keycloak-subject
//       ↓                                ↓
//    find existing User              find existing User
//       ↓                                ↓
//    same database user              same database user
//       │                                │
//       └──────── booking race ──────────┘


// Then the actual concurrency logic you're trying to test happens:

//    Thread A                                Thread B
//
//    find room FOR UPDATE
//       ↓
//    gets lock 🔒
//
//                                            find room FOR UPDATE
//                                                                                  ↓
//                                            waits...
//
//    check overlap
//    → none
//
//    save booking
//    commit
//    unlock 🔓
//            ↓
//                                            gets room lock
//                                                                                  ↓
//                                            check overlap
//                                                                                  ↓
//                                            booking exists
//                                                                                  ↓
//                                            BookingConflictException


//Expected:
//
//A succeeds ✅
//B gets BookingConflictException ❌
//
//OR
//
//B succeeds ✅
//A gets BookingConflictException ❌
//
//Total bookings in DB = 1


//
//The key new object is:
//
//CountDownLatch
//
//        Without it, thread A might finish before thread B even starts, which wouldn't really test concurrency.
//
//Instead:
//
//Thread A ── ready ─┐
//        │
//Thread B ── ready ─┤
//        ↓
//startSignal
//                   ↓
//BOTH RELEASED
//AT SAME TIME
//
//Then PostgreSQL should do:
//
//Thread A
//  ↓
//SELECT room FOR UPDATE
//  ↓
//gets lock 🔒
//        ↓
//checks availability
//  ↓
//saves booking
//  ↓
//COMMIT
//  ↓
//unlock
//
//
//Thread B
//  ↓
//SELECT room FOR UPDATE
//  ↓
//WAIT...
//        ↓
//A commits
//  ↓
//B gets lock
//  ↓
//checks availability again
//  ↓
//sees A's booking
//        ↓
//BookingConflictException