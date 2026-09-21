package com.codewithmosh.store.booking;


import com.codewithmosh.store.room.Room;
import com.codewithmosh.store.room.RoomRepository;
import com.codewithmosh.store.user.User;
import com.codewithmosh.store.user.UserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Testcontainers repository integration tests

@DataJpaTest // Start only the parts of Spring required for testing JPA entities and repositories.
@Testcontainers // Enables the JUnit/Testcontainers integration.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // This tells Spring Boot not to replace the database connection with an in-memory database. We want to use the PostgreSQL Testcontainer instead.
class BookingRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired // Autowired means Spring will inject the BookingRepository bean into this test class.
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldDetectOverlappingBooking() {

        User user = new User();
        user.setName("Milena");
        user.setEmail("milena@example.com");

        user = userRepository.saveAndFlush(user);

        Room room = new Room();
        room.setName("Meeting Room A");
        room.setCapacity(10);
        room = roomRepository.saveAndFlush(room);

        LocalDateTime existingStart = LocalDateTime.of(2030, 1, 10, 10, 0);
        LocalDateTime existingEnd = LocalDateTime.of(2030, 1, 10, 11, 0);

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartTime(existingStart);
        booking.setEndTime(existingEnd);

        bookingRepository.saveAndFlush(booking);

        boolean overlapping =
                bookingRepository.existsOverlappingBooking(
                        room.getId(),
                        LocalDateTime.of(2030, 1, 10, 10, 30),
                        LocalDateTime.of(2030, 1, 10, 11, 30)
                );

        assertTrue(overlapping);
    }

    //    Below we're testing:
    //
    //    Existing:
    //            10:00 ─────── 11:00
    //
    //    Requested:
    //            11:00 ─────── 12:00
    //
    //    No overlap ✅
    @Test
    void shouldNotDetectAdjacentBookingAsOverlap() {

        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");

        user = userRepository.saveAndFlush(user);

        Room room = new Room();
        room.setName("Meeting Room B");
        room.setCapacity(8);

        room = roomRepository.saveAndFlush(room);

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setRoom(room);

        booking.setStartTime(LocalDateTime.of(2030, 1, 10, 10, 0));
        booking.setEndTime(LocalDateTime.of(2030, 1, 10, 11, 0));

        bookingRepository.saveAndFlush(booking);

        boolean overlapping =
                bookingRepository.existsOverlappingBooking(
                        room.getId(),

                        LocalDateTime.of(2030, 1, 10, 11, 0),
                        LocalDateTime.of(2030, 1, 10, 12, 0)
                );

        assertFalse(overlapping);
    }
}

//Normally we have:
//
//spring.datasource.url=${DATABASE_URL}
//
//and Docker Compose provides:
//
//DATABASE_URL=jdbc:postgresql://java_db:5432/postgres
//
//For this test, @ServiceConnection says:
//
//Ignore the normal database connection details and connect Spring to this temporary PostgreSQL container.
//
//Spring Boot documents that service-connection details take precedence over connection properties.
//It can also provide both JDBC and Flyway connection details from a database Testcontainer.
//
//So we don't need to manually construct a JDBC URL.


//Why use saveAndFlush() here?
//
//Earlier I recommended ordinary:
//
//        bookingRepository.save(booking);
//
//for your production BookingService.
//
//        That's still correct.
//
//In this repository integration test, I'm deliberately using:
//
//saveAndFlush()
//
//because I want Hibernate to actually execute the SQL against PostgreSQL before we perform the query.
//
//So:
//
//Production:
//save()
//
//Integration test setup:
//saveAndFlush()
//
//is perfectly reasonable.



//Flyway should participate too
//
//This is one of the nicest parts of this test.
//
//When the test starts:
//
//JUnit
// ↓
//Testcontainers
// ↓
//starts PostgreSQL 17
//        ↓
//Spring @ServiceConnection
// ↓
//Flyway connects
// ↓
//V1 users
// ↓
//V2 rooms
// ↓
//V3 bookings
// ↓
//Hibernate/JPA
// ↓
//BookingRepositoryTest
//
//So we're testing against the same Flyway schema definitions as your real application, rather than letting Hibernate invent a test schema.
//
//That means the test can catch mismatches between:
//
//Booking.java
//
//        and
//
//V3__create_bookings_table.sql


//You don't need docker compose up
//
//For this test, just make sure Docker Desktop itself is running.
//
//Do not need:
//
//docker compose up
//
//Testcontainers handles PostgreSQL itself.


// By running the command:
// ./mvnw test

// Testcontainers successfully connected to Docker Desktop and started a temporary PostgreSQL 17 container on a random local port.
// Flyway connected to that temporary database and automatically ran V1, V2, and V3.
// Hibernate then used that same PostgreSQL database and executed your real INSERT and overlap SELECT queries.
// Both repository tests passed, your four Mockito tests passed, and your two plain JUnit tests passed. Overall: 9 tests, 0 failures, 0 errors.

// So we've now completed:
// 5.1 Plain JUnit                ✅
// 5.2 Mockito service tests      ✅
// 5.3 PostgreSQL + Testcontainers ✅

// Next: 5.4 API tests
// Now we test from the HTTP boundary: