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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test booking ownership authorization.

// This is an integration test.

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class BookingOwnershipAuthorizationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    private User userA;
    private User userB;
    private Booking booking;

    @BeforeEach
    void setUp() {
        /*
         * Delete in this order because bookings reference
         * users and rooms through foreign keys.
         */
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        userA = new User();
        userA.setName("User A");
        userA.setEmail("user-a@example.com");
        userA.setKeycloakSubject("keycloak-user-a");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setName("User B");
        userB.setEmail("user-b@example.com");
        userB.setKeycloakSubject("keycloak-user-b");
        userB = userRepository.save(userB);

        Room room = new Room();
        room.setName("Ownership Test Room");
        room.setCapacity(4);
        room = roomRepository.save(room);

        booking = new Booking();
        booking.setUser(userA);
        booking.setRoom(room);
        booking.setStartTime(LocalDateTime.now().plusDays(10));
        booking.setEndTime(LocalDateTime.now().plusDays(10).plusHours(1));

        booking = bookingRepository.save(booking);
    }

    @Test
    void anotherUserCannotDeleteBooking() throws Exception {

        mockMvc.perform(
                    delete(
                        "/api/bookings/{id}",
                        booking.getId()
                    )
                    .with(
                        jwt().jwt(jwt ->
                            jwt
                                .subject("keycloak-user-b") // userB is trying to delete userA's booking
                                .claim("preferred_username", "user-b")
                                .claim("email", "user-b@example.com")
                        )
                    )
                )
                .andExpect(status().isForbidden());

        /*
         * Important:
         * 403 alone isn't enough.
         * Make sure the booking was NOT deleted.
         */
        boolean bookingStillExists = bookingRepository.existsById(booking.getId());
        assertThat(bookingStillExists).isTrue();
    }

    @Test
    void ownerCanDeleteOwnBooking() throws Exception {

        mockMvc.perform(
                    delete(
                        "/api/bookings/{id}",
                        booking.getId()
                    )
                    .with(
                        jwt().jwt(jwt ->
                            jwt
                                .subject("keycloak-user-a")
                                .claim("preferred_username", "user-a")
                                .claim("email", "user-a@example.com")
                        )
                    )
                )
                .andExpect(status().isNoContent());

        boolean bookingStillExists = bookingRepository.existsById(booking.getId());
        assertThat(bookingStillExists).isFalse();
    }

    @Test
    void unauthenticatedUserCannotDeleteBooking() throws Exception {

        mockMvc.perform(
                    delete(
                        "/api/bookings/{id}",
                        booking.getId()
                    )
                )
                .andExpect(status().isUnauthorized());

        boolean bookingStillExists = bookingRepository.existsById(booking.getId());
        assertThat(bookingStillExists).isTrue();
    }
}