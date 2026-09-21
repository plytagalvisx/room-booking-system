package com.plytagalvisx.roombooking.room;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

//@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) // We're adding a locking method to RoomRepository
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}

// For PostgreSQL, this effectively means:
// SELECT ...
// FROM rooms
// WHERE id = ?
// FOR UPDATE;
// PostgreSQL locks that room row until the transaction finishes.