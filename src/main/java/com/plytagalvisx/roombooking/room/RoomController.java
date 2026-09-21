package com.plytagalvisx.roombooking.room;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    // Dependency injection of the RoomService to handle business logic related to rooms.
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public Room getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomService.createRoom(room);
    }

    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable Long id, @RequestBody Room room) {
        // TODO: Don't forget to handle errors here as well
        return roomService.updateRoom(id, room);
    }

    @DeleteMapping("/{id}")
    public String deleteRoom(@PathVariable Long id) {
        try {
            roomService.getRoomById(id); // Check if the room exists before deleting.
            roomService.deleteRoom(id);
            return "Room deleted successfully";
        } catch (Exception e) { // catches everything. Even when PostgreSQL crashes
            // TODO: Handle specific failures instead of catching Exception (because we can catch other exceptions here
            //  as well, not just the user not found exception)
            return "Error deleting room"; // Room not found or other error
        }
    }
}