package com.plytagalvisx.roombooking.booking;

import com.plytagalvisx.roombooking.booking.dto.BookingRequest;
import com.plytagalvisx.roombooking.booking.dto.BookingResponse;
import com.plytagalvisx.roombooking.common.exception.BookingConflictException;
import com.plytagalvisx.roombooking.common.exception.ResourceNotFoundException;
import com.plytagalvisx.roombooking.room.Room;
import com.plytagalvisx.roombooking.room.RoomRepository;
import com.plytagalvisx.roombooking.user.User;
import com.plytagalvisx.roombooking.user.AuthenticatedUserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Mockito service-testing

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

//    @Mock
//    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    // previously, our Mockito tests mocked UserRepository.
    // Our unit test should mock AuthenticatedUserService, not pretend the request contains a user ID.
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private BookingService bookingService;

    private static final String SUBJECT = "test-keycloak-subject";
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";

    @Test
    void shouldRejectOverlappingBooking() {

        User user = new User();
        user.setId(1L);

        Room room = new Room();
        room.setId(1L);

        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

        // when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authenticatedUserService.getOrCreateUser(SUBJECT, USERNAME, EMAIL)).thenReturn(user);
        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(1L, start, end)).thenReturn(true);

        // BookingRequest request = new BookingRequest(1L,  1L, start, end);
        BookingRequest request = new BookingRequest(1L, start, end);

        // assertThrows(BookingConflictException.class, () -> bookingService.createBooking(request));
        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(request, SUBJECT, USERNAME, EMAIL));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldCreateBookingWhenRoomIsAvailable() {

        User user = new User();
        user.setId(1L);

        Room room = new Room();
        room.setId(1L);

        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

        BookingRequest request = new BookingRequest(1L, start, end);

//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authenticatedUserService.getOrCreateUser(SUBJECT, USERNAME, EMAIL)).thenReturn(user);
        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        // Room is available
        when(bookingRepository.existsOverlappingBooking(1L, start, end)).thenReturn(false);
        // Simulate the database saving the booking
        // When the service tries to save any Booking, intercept it.
        // Then simulate what the real database/JPA would do:
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking booking = invocation.getArgument(0);
                    booking.setId(10L);
                    return booking;
                });

        BookingResponse response = bookingService.createBooking(request, SUBJECT, USERNAME, EMAIL);

        assertEquals(10L, response.id());
        assertEquals(1L, response.userId());
        assertEquals(1L, response.roomId());
        assertEquals(start, response.startTime());
        assertEquals(end, response.endTime());

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldRejectBookingWhenUserDoesNotExist() {

        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

        BookingRequest request = new BookingRequest(1L, start, end);

        // Pretend User 1 does not exist
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(authenticatedUserService.getOrCreateUser(SUBJECT, USERNAME, EMAIL)).thenThrow(new ResourceNotFoundException("User not found with subject: " + SUBJECT));

//        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request));
        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request, SUBJECT, USERNAME, EMAIL));

        // Since the user doesn't exist,
        // we should never even try to save a booking.
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldRejectBookingWhenRoomDoesNotExist() {

        User user = new User();
        user.setId(1L);

        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

        BookingRequest request = new BookingRequest(1L, start, end);

        // User exists
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authenticatedUserService.getOrCreateUser(SUBJECT, USERNAME, EMAIL)).thenReturn(user);
        // But Room 1 does not exist
        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

//        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request));
        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request, SUBJECT, USERNAME, EMAIL));

        verify(bookingRepository, never()).save(any(Booking.class));
    }
}

// This test does not connect to PostgreSQL:
//BookingService
//      ↓
//UserRepository mock
//RoomRepository mock
//BookingRepository mock
//      ✕
//PostgreSQL

// OBS! Spring Boot's test starter (spring-boot-starter-test) dependency already gives you JUnit and Mockito.