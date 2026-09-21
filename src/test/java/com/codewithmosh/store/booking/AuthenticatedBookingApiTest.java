package com.codewithmosh.store.booking;

import com.codewithmosh.store.room.Room;
import com.codewithmosh.store.room.RoomRepository;
import com.codewithmosh.store.user.User;
import com.codewithmosh.store.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// This is an integration test:

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test") // We don't need to do this for a pure unit test because that test doesn't start Spring. Later we can eliminate this repetition with a shared test annotation/base configuration. Don't introduce that abstraction yet.
class AuthenticatedBookingApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private Room room;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        userA = new User();
        userA.setName("User A");
        userA.setEmail("user-a@example.com");
        userA.setKeycloakSubject("subject-a");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setName("User B");
        userB.setEmail("user-b@example.com");
        userB.setKeycloakSubject("subject-b");
        userB = userRepository.save(userB);

        room = new Room();
        room.setName("API Test Room");
        room.setCapacity(5);
        room = roomRepository.save(room);
    }

    // Booking creation uses the JWT identity.
    //    JWT:
    //    sub = subject-a
    //       ↓
    //    AuthenticatedUserService
    //       ↓
    //    User A
    //       ↓
    //    Booking.user = User A
    @Test
    void authenticatedUserCreatesBookingAsThemselves() throws Exception {

        LocalDateTime start =
                LocalDateTime.now()
                        .plusDays(20)
                        .withSecond(0)
                        .withNano(0);

        LocalDateTime end = start.plusHours(1);

        String requestBody = """
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
                            .with(
                                jwt().jwt(jwt ->
                                    jwt
                                        .subject("subject-a")
                                        .claim("preferred_username", "user-a")
                                        .claim("email", "user-a@example.com")
                                )
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userA.getId()))
                .andExpect(jsonPath("$.roomId").value(room.getId()));

        var bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.getFirst().getUser().getId()).isEqualTo(userA.getId());
    }

    // Test an impersonation attempt.
    //    Authenticated as User A
    //    JSON says:
    //     "userId": User B
    //        ↓
    //    BookingRequest doesn't use userId
    //        ↓
    //    Spring uses JWT identity
    //        ↓
    //    booking belongs to User A ✅
    @Test
    void clientCannotChooseAnotherBookingOwner() throws Exception {

        LocalDateTime start =
                LocalDateTime.now()
                        .plusDays(21)
                        .withSecond(0)
                        .withNano(0);

        LocalDateTime end = start.plusHours(1);

        String maliciousRequest = """
            {
              "userId": %d,
              "roomId": %d,
              "startTime": "%s",
              "endTime": "%s"
            }
            """.formatted(
                userB.getId(),
                room.getId(),
                start,
                end
        );

        mockMvc.perform(
                        post("/api/bookings")
                            .with(
                                jwt().jwt(jwt ->
                                    jwt
                                        .subject("subject-a")
                                        .claim("preferred_username", "user-a")
                                        .claim("email", "user-a@example.com")
                                )
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(maliciousRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userA.getId()));

        Booking savedBooking = bookingRepository.findAll().getFirst();
        assertThat(savedBooking.getUser().getId()).isEqualTo(userA.getId());
        assertThat(savedBooking.getUser().getId()).isNotEqualTo(userB.getId());
    }


    // /bookings/me only returns your own bookings.
    @Test
    void myBookingsReturnsOnlyAuthenticatedUsersBookings() throws Exception {
        Booking bookingA = new Booking();
        bookingA.setUser(userA);
        bookingA.setRoom(room);
        bookingA.setStartTime(LocalDateTime.now().plusDays(30));
        bookingA.setEndTime(
                LocalDateTime.now()
                        .plusDays(30)
                        .plusHours(1)
        );
        bookingA = bookingRepository.save(bookingA);

        Booking bookingB = new Booking();
        bookingB.setUser(userB);
        bookingB.setRoom(room);
        bookingB.setStartTime(LocalDateTime.now().plusDays(31));
        bookingB.setEndTime(
                LocalDateTime.now()
                        .plusDays(31)
                        .plusHours(1)
        );
        bookingB = bookingRepository.save(bookingB);

        mockMvc.perform(
                        get("/api/bookings/me")
                            .with(
                                jwt().jwt(jwt ->
                                    jwt
                                        .subject("subject-a")
                                        .claim("preferred_username", "user-a")
                                        .claim("email", "user-a@example.com")
                                )
                            )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bookingA.getId()))
                .andExpect(jsonPath("$[0].userId").value(userA.getId()));
    }

    // Verify authentication is actually required.
    @Test
    void bookingCreationWithoutJwtReturns401() throws Exception {

        String json = """
            {
              "roomId": 1,
              "startTime": "2030-01-10T10:00:00",
              "endTime": "2030-01-10T11:00:00"
            }
            """;

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void myBookingsWithoutJwtReturns401() throws Exception {

        mockMvc.perform(
                        get("/api/bookings/me")
                )
                .andExpect(status().isUnauthorized());
    }

}

// POST /bookings
// without JWT → 401
//
// GET /bookings/me
// without JWT → 401


// What these tests now prove
// Our backend security coverage becomes much more complete:
//
// Authentication
// ├── POST booking without JWT → 401
//        └── GET /bookings/me without JWT → 401
//
// Identity
// └── JWT subject determines booking owner
//
// Impersonation protection
// └── client cannot choose another userId
//
// Data isolation
// └── /bookings/me returns only own bookings
//
// Ownership authorization
// ├── owner DELETE → 204
//        └── other user DELETE → 403
//
// Role authorization
// ├── USER POST room → 403
//        └── ADMIN POST room → success