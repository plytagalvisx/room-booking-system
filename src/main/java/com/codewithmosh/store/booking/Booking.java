package com.codewithmosh.store.booking;

import com.codewithmosh.store.room.Room;
import com.codewithmosh.store.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// Booking feature ties together your User and Room features and introduces relationships, validation,
// business rules, and proper HTTP status codes

// OBS! I don't need to add List<Booking> to User or Room yet.
// Keeping the relationship unidirectional is simpler for my learning project.

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Many bookings can point to the same user.
    @JoinColumn(name = "user_id", nullable = false) // fetch = FetchType.LAZY = a Booking doesn't automatically need to load every detail about the User and Room immediately.
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Many bookings can point to the same room (with different time slots).
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // Getters and Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Room getRoom() { return room; }

    public void setRoom(Room room) { this.room = room; }

    public LocalDateTime getStartTime() { return startTime; }

    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }

    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
