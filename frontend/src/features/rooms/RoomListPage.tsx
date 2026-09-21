// For now we'll hard-code fake data:

// import { useEffect, useState } from "react";
import RoomCard  from "./RoomCard";
import { getRooms } from "./roomApi";
// import type { Room } from "./roomTypes";
import { useQuery } from "@tanstack/react-query";

// import BookingForm from "../bookings/BookingForm";
// import MyBookingsPage from "../bookings/MyBookingsPage";

function RoomListPage() {
    // const rooms: Room[] = [ // rooms is an array containing Room objects.
    //     { id: 1, name: "Conference Room A", capacity: 8 },
    //     { id: 2, name: "Meeting Room B", capacity: 4 },
    //     { id: 3, name: "Large Meeting Room", capacity: 16 },
    // ];

    // This page can be in several states: loading, success -> display rooms, or error.
    // const [rooms, setRooms] = useState<Room[]>([]); // Initialize rooms state as an empty array. Current Room[] stored by the component. setRooms(...) updates that state.
    // const [loading, setLoading] = useState(true); // Initialize loading state as true.
    // const [error, setError] = useState<string | null>(null); // Initialize error state as null.
    //
    // useEffect(() => {
    //     async function loadRooms() {
    //         try {
    //             const data = await getRooms(); // Fetch rooms from the API.
    //             setRooms(data); // Update rooms state with fetched data. React notices the state changed and renders the room cards again.
    //         } catch (error) {
    //             setError("Failed to fetch rooms");
    //         } finally {
    //             setLoading(false);
    //         }
    //     }
    //
    //     loadRooms(); // Call the async function to load rooms.
    // }, []); // Empty dependency array means this effect runs once after the initial render.

    // With the help of TanStack Query, we can remove the state management and useEffect logic above,
    // and instead use the useQuery hook to fetch data and manage loading and error states automatically, like so:
    const { // TanStack Query, manage the server state associated with the rooms request.
        data: rooms = [],
        isLoading,
        isError,
        error,
    } = useQuery({
        queryKey: ["rooms"], // Think of that as the identity of this server data.
        queryFn: getRooms,
        staleTime: 60_000, // Treat these rooms as fresh for 60 seconds.
    });

    if (isLoading) {
        return <p>Loading rooms...</p>; // Show loading message while fetching data.
    }

    if (isError) {
        return <p>Error: {error.message}</p>; // Show error message if fetching fails.
    }

    return (
        <main>
            <h1>Rooms</h1>

            {rooms.length === 0 && (
                <p>No rooms found.</p>
            )}

            {rooms.map((room) => (
                <RoomCard
                    key={room.id} // React requires a unique key for each child in a list.
                    room={room} // This is the prop we defined in RoomCardProps.
                />
            ))}

            {/* React Router will take care of these from now on: */}
            {/*<BookingForm rooms={rooms} />*/}
            {/*<hr />*/}
            {/*<MyBookingsPage />*/}

        </main>
    );
}

export default RoomListPage;

// Rendering collections:
//   rooms.map(...)
//
// turns our array into components:
//
//    rooms[]
//        ↓
//     map()
//        ↓
//     RoomCard
//     RoomCard
//     RoomCard

// We have our first interesting props relationship:

// RoomListPage
// │
// ├── RoomCard
// ├── RoomCard
// ├── RoomCard
// │
// └── BookingForm
//        ↑
//        │
//      rooms prop

// At this stage we want to see three hard-coded rooms in the browser.

// Don't connect Spring yet. Once this static version renders correctly,
// step 6.4 is where we'll remove that hard-coded Room[] and replace it with:

// GET /api/rooms
//       ↓
// fetch()
//       ↓
// Spring Boot
//       ↓
// PostgreSQL
//       ↓
// JSON
//       ↓
// Room[]
//       ↓
// React

// In other words:

// React
//   ↓
// fetch("http://localhost:8080/api/rooms")
//   ↓
// RoomController
//   ↓
// RoomService
//   ↓
// RoomRepository
//   ↓
// PostgreSQL
//   ↓
// JSON response
//   ↓
// React state
//   ↓
// RoomCard components

// That's where we'll introduce useState, useEffect, async/await, CORS, and inspect the real HTTP request in DevTools.


// Why useEffect is appropriate here?:
//
// We previously learned that modern React tries not to use useEffect for everything.
//
// But this is a legitimate use:
//
//     useEffect(() => {
//         ...
//     }, []);
//
// because we're synchronizing React with an external system:
//
// React component
//       ↕
// Spring HTTP API
//
//
// The empty: []
//
// means the effect runs when this component is mounted rather than every time it renders
//
// So:
//
// RoomListPage appears
//        ↓
// useEffect
//        ↓
// loadRooms()
//        ↓
// getRooms()
//        ↓
// fetch()



// We see the whole thing:

// RoomListPage
//     ↓
// useEffect
//     ↓
// getRooms()
//     ↓
// fetch()
//     ↓
// GET /api/rooms
//     ↓
// RoomController
//     ↓
// RoomService
//     ↓
// RoomRepository
//     ↓
// PostgreSQL
//     ↓
// List<Room>
//     ↓
// Spring serializes to JSON
//     ↓
// HTTP 200
//     ↓
// response.json()
//     ↓
// Room[]
//     ↓
// setRooms(data)
//     ↓
// React re-renders
//     ↓
// RoomCard × N