// import { useEffect, useState } from "react";
import BookingForm from "./BookingForm";
import { getRooms } from "../rooms/roomApi";
// import type { Room } from "../rooms/roomTypes";
import { useQuery } from "@tanstack/react-query";

function BookingPage() {
    // const [rooms, setRooms] = useState<Room[]>([]);
    // const [loading, setLoading] = useState(true);
    // const [error, setError] = useState<string | null>(null);
    //
    // useEffect(() => {
    //     async function loadRooms() {
    //         try {
    //             const data = await getRooms(); // returns a Promise that resolves to an array of Room objects. We await it to get the actual data.
    //             setRooms(data);
    //         } catch (error) {
    //             if (error instanceof Error) {
    //                 setError(error.message);
    //             } else {
    //                 setError("Failed to load rooms");
    //             }
    //         } finally {
    //             setLoading(false);
    //         }
    //     }
    //     loadRooms();
    // }, []);


    // OBS! So if we visit:
    //  /rooms
    //    ↓
    // GET /api/rooms
    //
    // then immediately
    //
    // /book
    //
    // TanStack Query can reuse the cached rooms instead of immediately requesting them again.
    // That's our first practical experience with server-state caching.
    const {
        data: rooms = [],
        isLoading,
        isError,
        error,
    } = useQuery({
        queryKey: ["rooms"],
        queryFn: getRooms,
        staleTime: 60_000, // 1 minute
    });

    if (isLoading) {
        return <p>Loading rooms...</p>;
    }

    if (isError) {
        return <p>Error: {error.message}</p>
    }

    return (
        <main>
            <h1>Book a Room</h1>

            <BookingForm rooms={rooms} />
        </main>
    );
}

export default BookingPage;