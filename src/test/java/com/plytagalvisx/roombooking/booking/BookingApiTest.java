package com.plytagalvisx.roombooking.booking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;

import com.plytagalvisx.roombooking.user.User;
import com.plytagalvisx.roombooking.user.UserRepository;
import com.plytagalvisx.roombooking.room.Room;
import com.plytagalvisx.roombooking.room.RoomRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

// I recommend making these real integration API tests, rather than mocking BookingService, because you've already learned mocking separately.

// BookingApiTest starts a real temporary PostgreSQL 17 container, Flyway applies all three migrations, Spring initializes MockMvc, and the API test passes.

// API integration tests

// This is an integration test

@SpringBootTest // This starts the entire Spring application, including controllers, services, repositories, validation, exception handling, and Flyway migrations.
@AutoConfigureMockMvc // This starts MockMvc, which allows us to test the API without starting a real HTTP server.
@Testcontainers // This starts a real PostgreSQL 17 container for the duration of the test.
@ActiveProfiles("test")
class BookingApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    // At this point I would also add a small @BeforeEach cleanup so each API test starts with an empty database:
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private static RequestPostProcessor authenticatedUser(String subject, String username, String email) {
        return jwt().jwt(jwt -> jwt
                .subject(subject)
                .claim("preferred_username", username)
                .claim("email", email)
        );
    }

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    // First test: invalid input → 400

    // This test verifies several pieces at once:
    //
    //    JSON
    // ↓
    //    BookingRequest
    // ↓
    //    BookingController
    // ↓
    //    BookingService
    // ↓
    //    IllegalArgumentException
    // ↓
    //    ApiExceptionHandler
    // ↓
    //    HTTP 400 (Bad Request)
    @Test
    void shouldReturn400ForInvalidBookingRequest() throws Exception {

        String json = """
            {
                "roomId": 1,
                "startTime": "2030-01-10T12:00:00",
                "endTime": "2030-01-10T11:00:00"
            }
            """;

        mockMvc.perform(
                        post("/api/bookings")
                                .with(authenticatedUser(
                                        "test-subject",
                                        "test-user",
                                        "test@example.com"
                                ))
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    // Successful booking → 201 Created:
    //    This tests:
    //
    //    POST /api/bookings
    //        ↓
    //    valid User
    //    valid Room
    //    room available
    //        ↓
    //    Booking created
    //        ↓
    //     201 Created
    @Test
    void shouldReturn201WhenBookingIsCreated() throws Exception {

        User user = new User();
        user.setName("Milena");
        user.setEmail("test@example.com");
        user.setKeycloakSubject("test-subject");
        userRepository.save(user);
//        user.setEmail("milena@example.com");
//        user = userRepository.saveAndFlush(user);

        Room room = new Room();
        room.setName("Meeting Room A");
        room.setCapacity(10);
        room = roomRepository.saveAndFlush(room);

        String json = """
            {
                "roomId": %d,
                "startTime": "2030-01-10T10:00:00",
                "endTime": "2030-01-10T11:00:00"
            }
            """.formatted(room.getId());

        mockMvc.perform(
                        post("/api/bookings")
                                .with(authenticatedUser(
                                        "test-subject",
                                        "test-user",
                                        "test@example.com"
                                ))

                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.roomId").value(room.getId()))
                .andExpect(jsonPath("$.startTime").value("2030-01-10T10:00:00"))
                .andExpect(jsonPath("$.endTime").value("2030-01-10T11:00:00"));
    }

    // Missing user → 404 Not Found:
//    @Test
//    void shouldReturn404WhenUserDoesNotExist() throws Exception {
//
//        Room room = new Room();
//        room.setName("Meeting Room A");
//        room.setCapacity(10);
//        room = roomRepository.saveAndFlush(room);
//
//        String json = """
//            {
//                "userId": 99999,
//                "roomId": %d,
//                "startTime": "2030-01-10T10:00:00",
//                "endTime": "2030-01-10T11:00:00"
//            }
//            """.formatted(room.getId());
//
//        mockMvc.perform(
//                        post("/api/bookings")
//                                .contentType("application/json")
//                                .content(json)
//                )
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.title").value("Resource not found"));
//    }

    @Test
    void shouldCreateApplicationUserFromAuthenticatedJwt() throws Exception {

        Room room = new Room();
        room.setName("Meeting Room A");
        room.setCapacity(10);
        room = roomRepository.saveAndFlush(room);

        LocalDateTime start =
                LocalDateTime.now()
                        .plusDays(10)
                        .withSecond(0)
                        .withNano(0);

        LocalDateTime end = start.plusHours(1);

        String json = """
            {
                "roomId": %d,
                "startTime": "%s",
                "endTime": "%s"
            }
            """.formatted(
                room.getId(),
                start,
                end
        );

        mockMvc.perform(
                        post("/api/bookings")
                                .with(authenticatedUser(
                                        "new-subject",
                                        "new-user",
                                        "new-user@example.com"
                                ))
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isCreated());

        assertThat(userRepository.findByKeycloakSubject("new-subject")).isPresent();
    }

    // Missing room → 404 Not Found:
    @Test
    void shouldReturn404WhenRoomDoesNotExist() throws Exception {

        User user = new User();
        user.setName("Milena");
//        user.setEmail("milena@example.com");
        user.setEmail("test@example.com");
        user.setKeycloakSubject("test-subject");
        userRepository.save(user);
//        user = userRepository.saveAndFlush(user);

        String json = """
            {
                "roomId": 99999,
                "startTime": "2030-01-10T10:00:00",
                "endTime": "2030-01-10T11:00:00"
            }
            """;

        mockMvc.perform(
                        post("/api/bookings")
                                .with(authenticatedUser(
                                        "test-subject",
                                        "test-user",
                                        "test@example.com"
                                ))
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    // Overlapping booking → 409 Conflict:
    //    This tests your whole conflict path:
    //
    //    POST
    // ↓
    //    Controller
    // ↓
    //    BookingService
    // ↓
    //    PESSIMISTIC_WRITE Room lock
    // ↓
    //    overlap query
    // ↓
    //    BookingConflictException
    // ↓
    //    ApiExceptionHandler
    // ↓
    //  409 Conflict
    @Test
    void shouldReturn409WhenBookingOverlaps() throws Exception {

        User user = new User();
        user.setName("Milena");
        user.setEmail("test@example.com");
        user.setKeycloakSubject("test-subject");
        userRepository.save(user);
//        user.setEmail("milena@example.com");
//        user = userRepository.saveAndFlush(user);

        Room room = new Room();
        room.setName("Meeting Room A");
        room.setCapacity(10);
        room = roomRepository.saveAndFlush(room);

        Booking existingBooking = new Booking();
        existingBooking.setUser(user);
        existingBooking.setRoom(room);
        existingBooking.setStartTime(LocalDateTime.of(2030, 1, 10, 10, 0));
        existingBooking.setEndTime(LocalDateTime.of(2030, 1, 10, 11, 0));
        bookingRepository.saveAndFlush(existingBooking);

        String json = """
            {
                "roomId": %d,
                "startTime": "2030-01-10T10:30:00",
                "endTime": "2030-01-10T11:30:00"
            }
            """.formatted(room.getId());

        mockMvc.perform(
                        post("/api/bookings")
                                .with(authenticatedUser(
                                        "test-subject",
                                        "test-user",
                                        "test@example.com"
                                ))
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Booking conflict"));
    }

    // Delete booking → 204 No Content:
    @Test
    void shouldReturn204WhenBookingIsDeleted() throws Exception {

        User user = new User();
        user.setName("Milena");
        user.setEmail("test@example.com");
        user.setKeycloakSubject("test-subject");
        user = userRepository.saveAndFlush(user);

        Room room = new Room();
        room.setName("Meeting Room A");
        room.setCapacity(10);
        room = roomRepository.saveAndFlush(room);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartTime(LocalDateTime.of(2030, 1, 10, 10, 0));
        booking.setEndTime(LocalDateTime.of(2030, 1, 10, 11, 0));
        booking = bookingRepository.saveAndFlush(booking);

        Long bookingId = booking.getId();

        mockMvc.perform(
                delete("/api/bookings/{id}", bookingId)
                    .with(authenticatedUser(
                            "test-subject",
                            "test-user",
                            "test@example.com"
                    ))
                )
                .andExpect(status().isNoContent());

        assertFalse(bookingRepository.existsById(bookingId));
    }

}


//The cases we want are:
//
//POST valid booking          → 201 Created
//POST invalid dates          → 400 Bad Request
//POST nonexistent user       → 404 Not Found
//POST nonexistent room       → 404 Not Found
//POST overlapping booking    → 409 Conflict
//GET existing booking        → 200 OK
//DELETE booking              → 204 No Content


//The difference from your repository test is:
//
//@DataJpaTest
//→ only JPA/repository layer
//
//versus:
//
//@SpringBootTest
//→ entire Spring application
//
//So now Spring loads:
//
//Controller ✅
//Service ✅
//Repository ✅
//Validation ✅
//Exception handler ✅
//Flyway ✅
//PostgreSQL ✅


//Our BookingApiTest should now cover
//shouldReturn400ForInvalidBookingRequest()  ✅ existing
//
//shouldReturn201WhenBookingIsCreated()
//        ↓
//                201 Created
//
//shouldReturn404WhenUserDoesNotExist()
//        ↓
//                404 Not Found
//
//shouldReturn404WhenRoomDoesNotExist()
//        ↓
//                404 Not Found
//
//shouldReturn409WhenBookingOverlaps()
//        ↓
//                409 Conflict
//
//shouldReturn204WhenBookingIsDeleted()
//        ↓
//                204 No Content

// We're testing not just controllers, but real JSON → validation → service → transaction/locking →
// JPA → PostgreSQL → exception handling → HTTP response behavior.