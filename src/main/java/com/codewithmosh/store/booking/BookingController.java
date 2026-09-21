package com.codewithmosh.store.booking;

import com.codewithmosh.store.booking.dto.BookingRequest;
import com.codewithmosh.store.booking.dto.BookingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBooking(id);
        return ResponseEntity.ok(booking);
    }

    // OBS! We now have: GET /api/bookings/user/5. Later, after we implement authentication,
    // we'd prefer something like: GET /api/bookings/me because the backend will know who is logged in.
//    @GetMapping("/user/{userId}")
//    public List<BookingResponse> getBookingsForUser(@PathVariable Long userId) {
//        return bookingService.getBookingsForUser(userId);
//    }

    @GetMapping("/me")
    public List<BookingResponse> getMyBookings(@AuthenticationPrincipal Jwt jwt) {
        return bookingService.getBookingsForAuthenticatedUser(jwt.getSubject(), jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("email"));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request, @AuthenticationPrincipal Jwt jwt) {
        String subject = jwt.getSubject();
        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");

        BookingResponse booking = bookingService.createBooking(request, subject, username, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        // Alternative:
//      return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deleteBooking(@PathVariable Long bookingId, @AuthenticationPrincipal Jwt jwt) {
//        bookingService.deleteBooking(bookingId, jwt.getSubject(), jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("email"));
//    }

    // Alternative way to handle DELETE with ResponseEntity:
    // Both approaches do the exact same thing under the hood: they return
    // a 204 No Content HTTP status code with an empty response body.
    // The choice between them comes down to cleanliness versus flexibility.
    @DeleteMapping("/{id}") // id is the booking ID to delete
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        bookingService.deleteBooking(id, jwt.getSubject(), jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("email"));
        return ResponseEntity.noContent().build();
    }
}

// Now look at where identity comes from:
//    JWT
//    │
//    ├── sub
//    ├── preferred_username
//    └── email
//         │
//         ▼
//    BookingController
//         │
//         ▼
//    BookingService
//         │
//         ▼
//    AuthenticatedUserService
//         │
//         ▼
//    User entity
// There is no browser-supplied user ID anywhere in that chain.