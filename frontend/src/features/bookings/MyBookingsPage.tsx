// import { useState } from "react";
import { getMyBookings, deleteBooking } from "./bookingApi";
// import type { BookingResponse } from "./bookingTypes";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

function MyBookingsPage() {
    // const [userId, setUserId] = useState(""); // Temporary user ID input for demonstration purposes
    // const [bookings, setBookings] = useState<BookingResponse[]>([]);
    // const [loading, setLoading] = useState(false);
    // const [error, setError] = useState<string | null>(null);
    // const [deletingId, setDeletingId] = useState<number | null>(null); // deletingId tells React which booking is currently being cancelled. Change to deleteMutation.isPending.

    // TanStack Query owns these pieces above of server state now.

    // This one is more interesting because the query depends on a user ID.
    // const [userId, setUserId] = useState(""); // what's currently inside the text box
    // const [selectedUserId, setSelectedUserId] = useState<number | null>(null); // the user whose bookings we actually want to fetch
    // Without this userId distinction, TanStack Query could fetch every time the user types: 5, 52, 527 in the input box.
    // Instead, we only want to fetch when the user clicks the "Load Bookings" button.

    const {
        data: bookings = [],
        isLoading,
        isError,
        error,
    } = useQuery({ // our query is to fetch bookings for a specific selected (aka authenticated) user ID and cache that data.
        // queryKey: ["bookings", selectedUserId],
        queryKey: ["bookings", "me"],
        // queryFn: () => getBookingsForUser(selectedUserId!), // The exclamation mark tells TypeScript that we know selectedUserId is not null here. We only call this function when selectedUserId is not null.
        queryFn: getMyBookings, // Or: queryFn: () => getMyBookings(),
        // enabled: selectedUserId !== null, // Only fetch when selectedUserId is not null
    })
    // For user 5: ["bookings", 5]
    // For user 12: ["bookings", 12]
    // These represent different cached data.

    // This updated handler now only selects the user:
    // function handleLoadBookings() {
    //     if (!userId) {
    //         return;
    //     }
    //     setSelectedUserId(Number(userId));
    // }

    // OBS!
    // We use useQuery() when we want to fetch data from the server and cache it.
    // We use useMutation() when we want to change server state (like creating, updating, or deleting data)
    // and then invalidate the relevant queries so that they refetch the updated data.

    const queryClient = useQueryClient();

    const deleteMutation = useMutation({
        mutationFn: deleteBooking,

        onSuccess: () => {
            queryClient.invalidateQueries({
                // queryKey: ["bookings", selectedUserId], // when we're going to make queries (e.g., fetching user bookings) in the future, we will invalidate to query the one that we already deleted/canceled.
                queryKey: ["bookings", "me"],
            });
        },
    });

    function handleDeleteBooking(bookingId: number) {
        deleteMutation.mutate(bookingId);
    }

    // This handler fetches my bookings manually:
    // async function handleLoadBookings() {
    //     setError(null);
    //     setBookings([]);
    //
    //     if (!userId) {
    //         setError("Enter a user ID.")
    //         return;
    //     }
    //
    //     try {
    //         setLoading(true);
    //
    //         const data = await getBookingsForUser(Number(userId));
    //         setBookings(data);
    //     } catch (error) {
    //         if (error instanceof Error) {
    //             setError(error.message);
    //         } else {
    //             setError("Something went wrong.");
    //         }
    //     } finally {
    //         setLoading(false);
    //     }
    // }

    // async function handleDeleteBooking(bookingId: number) {
    //     setError(null);
    //
    //     try {
    //         setDeletingId(bookingId);
    //         await deleteBooking(bookingId);
    //         setBookings((currentBookings) => currentBookings.filter((booking) => booking.id !== bookingId));
    //     } catch (error) {
    //         if (error instanceof Error) {
    //             setError(error.message);
    //         } else {
    //             setError("Something went wrong.");
    //         }
    //     } finally {
    //         setDeletingId(null);
    //     }
    // }

    return (
        <section>
            <h2>My Bookings</h2>

            {/*<div>*/}
            {/*    <label htmlFor="bookingUserId">*/}
            {/*        User ID*/}
            {/*    </label>*/}

            {/*    <input*/}
            {/*        id="bookingUserId"*/}
            {/*        type="number"*/}
            {/*        min="1"*/}
            {/*        value={userId}*/}
            {/*        onChange={(event) => setUserId(event.target.value)}*/}
            {/*    />*/}

            {/*    <button*/}
            {/*        type="button"*/}
            {/*        onClick={handleLoadBookings}*/}
            {/*        // disabled={loading}*/}
            {/*    >*/}
            {/*        Load Bookings*/}
            {/*        /!*{loading ? "Loading..." : "Load Bookings"}*!/*/}
            {/*    </button>*/}
            {/*</div>*/}

            {/*{error && <p>{error}</p>}*/}

            {/*{!loading && !error && bookings.length === 0 && (*/}
            {/*    <p>No bookings found.</p>*/}
            {/*)}*/}

            {isLoading && <p>Loading bookings...</p>}

            {/*{selectedUserId !== null && !isLoading && !isError && bookings.length === 0 && (<p>No bookings found.</p>)}*/}
            {!isLoading && !isError && bookings.length === 0 && (<p>No bookings found.</p>)}

            {isError && (<p>Failed to load bookings: {error.message}</p>)} {/*is for errors when loading bookings with useQuery.*/}

            {deleteMutation.isError && (<p>Failed to cancel booking: {deleteMutation.error.message}</p>)} {/*This is for errors when deleting a booking. Example: DELETE /api/bookings/5 → 404 / 500*/}

            {bookings.map((booking) => (
                <div key={booking.id}>
                    <h3>Booking #{booking.id}</h3>
                    <p>Room ID: {booking.roomId}</p>
                    <p>Start time: {booking.startTime}</p>
                    <p>End time: {booking.endTime}</p>
                    <button
                        type="button"
                        onClick={() => handleDeleteBooking(booking.id)} // We need the arrow function because we want to pass that particular booking's ID. We don't want: onClick={handleDeleteBooking(booking.id)}. Because that would call the function immediately when the React component renders, instead of when the button is clicked.
                        disabled={deleteMutation.isPending}
                        // disabled={deletingId === booking.id}
                    >
                        {deleteMutation.isPending ? "Cancelling..." : "Cancel Booking"} {/* One limitation: while any booking is being deleted, all buttons are disabled. That's okay for now. Later we could track the mutation variable to identify exactly which booking is being deleted. */}
                        {/*{deletingId === booking.id ? "Cancelling..." : "Cancel Booking"}*/}
                    </button>
                </div>
            ))}
        </section>
    );
}

export default MyBookingsPage;

// The flow is now:
// click
//   ↓
// setSelectedUserId(5)
//   ↓
// query key becomes ["bookings", 5]
//   ↓
// TanStack Query runs queryFn
//   ↓
// getBookingsForUser(5)
//   ↓
// GET /api/bookings/user/5


// Previously we manually did:
//
// setBookings((currentBookings) =>
//     currentBookings.filter(...)
// );
//
// Now we can tell TanStack Query:
//
// queryClient.invalidateQueries({
//     queryKey: ["bookings", selectedUserId],
// });


// Conceptually:
//
// DELETE booking 4
//        ↓
// 204 success
//        ↓
// TanStack Query:
//     "the cached bookings may now be outdated"
//        ↓
// invalidate ["bookings", 5]
//        ↓
// GET bookings again
//        ↓
// fresh DB state
//        ↓
// React updates

// That distinction is very important.

// Before:
//
// DELETE
// ↓
// manually modify React state


// Now:
//
// DELETE
// ↓
// server changed
// ↓
// mark corresponding server-state cache stale
// ↓
// refetch authoritative state