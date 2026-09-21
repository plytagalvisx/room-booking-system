package com.codewithmosh.store.booking;

import com.codewithmosh.store.booking.dto.BookingRequest;
import com.codewithmosh.store.booking.dto.BookingResponse;
import com.codewithmosh.store.common.exception.BookingConflictException;
import com.codewithmosh.store.common.exception.ForbiddenException;
import com.codewithmosh.store.common.exception.ResourceNotFoundException;

import com.codewithmosh.store.room.Room;
import com.codewithmosh.store.room.RoomRepository;
import com.codewithmosh.store.user.AuthenticatedUserService;
import com.codewithmosh.store.user.User;
import com.codewithmosh.store.user.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {
//    private final UserRepository userRepository; // BookingService uses Users table
    private final BookingRepository bookingRepository; // BookingService uses Bookings table
    private final RoomRepository roomRepository; // BookingService uses Rooms table
    private final AuthenticatedUserService authenticatedUserService; // BookingService uses AuthenticatedUserService to get the authenticated user

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, AuthenticatedUserService authenticatedUserService) {
//        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(booking -> new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getRoom().getId(),
                booking.getStartTime(),
                booking.getEndTime()
        )).toList();

        // Alternative:
//        return bookingRepository
//                .findAll()
//                .stream()
//                .map(this::toResponse)
//                .toList();
    }

    public BookingResponse getBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return toResponse(booking);
    }

    /**
     * Get all bookings for a specific user.
     *
     * @param subject The subject of the authenticated user (from JWT).
     * @param username The username of the authenticated user (from JWT).
     * @param email The email of the authenticated user (from JWT).
     * @return A list of booking responses.
     */
//    @Transactional(readOnly = true) // @Transactional ensures that all database operations succeed together or fail together as a single, atomic unit to preserve data integrity.
    @Transactional
    public List<BookingResponse> getBookingsForAuthenticatedUser(String subject, String username, String email) {
//    public List<BookingResponse> getBookingsForUser(Long userId) {

        // Check if the user exists. If not, throw a ResourceNotFoundException.
        // userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        // We don't need a user ID from the browser anymore.

        User user = authenticatedUserService.getOrCreateUser(subject, username, email);

        List<Booking> userBookings = bookingRepository.findByUser_IdOrderByStartTimeAsc(user.getId()); // userId);
        return userBookings.stream().map(userBooking -> new BookingResponse(
                userBooking.getId(),
                userBooking.getUser().getId(),
                userBooking.getRoom().getId(),
                userBooking.getStartTime(),
                userBooking.getEndTime()
        )).toList();

        // Alternative:
//        return bookingRepository
//                .findByUser_IdOrderByStartTimeAsc(user.getId()) // userId)
//                .stream()
//                .map(this::toResponse)
//                .toList();
    }

    // UPDATE: we change our booking creation method so it receives the authenticated identity from the JWT:
    @Transactional // This annotation ensures that the entire method runs within a single transaction, which is important for maintaining data integrity when multiple database operations are involved.
    public BookingResponse createBooking(BookingRequest request, String subject, String username, String email) { // subject, username, and email are obtained from the JWT token of the authenticated user, and they are used to identify the user making the booking request.
        // Business rule #1:
        // End must be after start.
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        // Business rule #2:
        // User must actually exist.
        // This is the first database operation (find a user in the users table in the DB by a given ID).
        // UPDATE: The user is now determined by the authenticated user, so we don't need to check if the user exists anymore.
        // User user = userRepository.findById(request.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));
        User user = authenticatedUserService.getOrCreateUser(subject, username, email);

        // Business rule #3:
        // Room must actually exist.
        // OBS! IMPORTANT: lock the room
        // This is the second database operation.
        Room room = roomRepository.findByIdForUpdate(request.roomId()) // roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.roomId()));

        // Business rule #4:
        // Room cannot already be booked during this interval.
        // This is the third database operation.
        boolean isOverlapping = bookingRepository.existsOverlappingBooking( // existsByRoom_IdAndStartTimeLessThanAndEndTimeGreaterThan(
                request.roomId(),
                request.startTime(),
                request.endTime()
        );
        if (isOverlapping) {
            throw new BookingConflictException("Room is already booked during this time interval.");
        }

        // Create the booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());

        // And this is the fourth database operation.
        Booking savedBooking = bookingRepository.save(booking); // We use pessimistic room-locking approach so we call save() instead of saveAndFlush() which is used for concurrency safety.

        return toResponse(savedBooking);

        // OBS!
        // In total, we have 4 database operations inside this method. Thus, this method involves
        // multiple database operations and is annotated with @Transactional to ensure that all
        // operations succeed or fail together as a single unit, maintaining data integrity.
        // For example, we wouldn't want to create a booking if the room is already booked during the requested time interval,
        // and we wouldn't want to leave the room locked if an error occurs during the booking creation process.
        // The @Transactional annotation ensures that if any of the database operations fail, all changes made during
        // the transaction will be rolled back, leaving the database in a consistent state.
    }

    public void deleteBooking(Long bookingId, String subject, String username, String email) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // TODO: Implement authorization check here to ensure that only the user who created the booking can delete it.
        // Get the user id from the loaded booking (which is ready to get deleted/canceled) and compare it with
        // the authenticated user id which is extracted by the given JWT's attributes (subject, username, email) as parameters.
        // If they don't match, throw a ForbiddenException.
        User authenticatedUser = authenticatedUserService.getOrCreateUser(subject, username, email);

        if (!booking.getUser().getId().equals(authenticatedUser.getId())) {
            throw new ForbiddenException("You are not allowed to cancel this booking.");
        }

        bookingRepository.delete(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getRoom().getId(),
                booking.getStartTime(),
                booking.getEndTime()
        );
    }
}
