import { useState, type FormEvent } from "react";
import type { Room } from "../rooms/roomTypes";
import { createBooking } from "./bookingApi.ts";
// import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useMutation } from "@tanstack/react-query";

type BookingFormProps = {
    rooms: Room[];
};

function BookingForm({ rooms }: BookingFormProps) {
    // const [userId, setUserId] = useState(""); // OBS! The User ID field is temporary because we haven't implemented authentication yet. Later, the logged-in user's identity will come from authentication instead of being typed manually.
    const [roomId, setRoomId] = useState("");
    const [startTime, setStartTime] = useState("");
    const [endTime, setEndTime] = useState("");

    const [error, setError] = useState<string | null>(null); // we keep useState for error messages because we want to display validation errors in the UI, which is a different kind of error than the one handled by TanStack Query's useMutation hook. The useMutation hook's error state is for API errors, while our error state is for validation errors and other client-side errors.
    // const [submitting, setSubmitting] = useState(false); // Change to createMutation.isPending
    const [success, setSuccess] = useState<string | null>(null);

    // const queryClient = useQueryClient();

    const createMutation = useMutation({
        mutationFn: createBooking,

        // createBooking() that contains the REST API POST fetch call returns a Promise that resolves to a
        // BookingResponse object. When the mutation is successful, we can access that object in the onSuccess callback.
        onSuccess: (booking) => {

            setSuccess(`Booking ${booking.id} created successfully.`);

            setStartTime("");
            setEndTime("");

            // Below code means that after a successful booking, any cached bookings for that user are marked
            // stale so TanStack Query knows that server state may have changed.
            // queryClient.invalidateQueries({ // here we're invalidating the query for the user's bookings so that it will refetch the updated list of bookings after a new booking is created.
            //     queryKey: ["bookings", Number(userId),], // bookings here acts like a cache key for the user's bookings. When we invalidate it, TanStack Query will refetch the bookings for that user.
            // });
            // UPDATE: remove this old invalidation for now if it's still here,
            // because we're about to replace the entire user-ID-based bookings query.
        },
    });

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setError(null);
        setSuccess(null);
        createMutation.reset();

        // if (!userId || !roomId || !startTime || !endTime) {
        if (!roomId || !startTime || !endTime) {
            setError("All fields are required.");
            return;
        }

        if (new Date(endTime) <= new Date(startTime)) {
            setError("End time must be after start time.");
            return;
        }

        createMutation.mutate({
            // userId: Number(userId),
            roomId: Number(roomId),
            startTime,
            endTime,
        });
    }

    // async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    //     event.preventDefault();
    //
    //     setError(null);
    //     setSuccess(null);
    //
    //     if (!userId || !roomId || !startTime || !endTime) {
    //         setError("All fields are required.");
    //         return;
    //     }
    //
    //     if (new Date(endTime) <= new Date(startTime)) {
    //         setError("End time must be after start time.");
    //         return;
    //     }
    //
    //     try {
    //         setSubmitting(true);
    //
    //         const booking = await createBooking({
    //             userId: Number(userId),
    //             roomId: Number(roomId),
    //             startTime,
    //             endTime,
    //         });
    //
    //         setSuccess(`Booking created successfully with ID: ${booking.id}`);
    //
    //         // Clear the form fields after successful booking
    //         setStartTime("");
    //         setEndTime("");
    //
    //     } catch (error) {
    //         if (error instanceof Error) {
    //             setError(error.message);
    //         } else {
    //             setError("Something went wrong.");
    //         }
    //     } finally {
    //         setSubmitting(false);
    //     }

        // console.log({
        //     userId: Number(userId),
        //     roomId: Number(roomId),
        //     startTime,
        //     endTime
        // })
    // }

    return (
        <form onSubmit={handleSubmit}>
            <h2>Book a room</h2>

            {/* We remove User ID from BookingForm */}
            {/*<div>*/}
            {/*    <label htmlFor="userId">User ID</label>*/}
            {/*    <input*/}
            {/*        id="userId"*/}
            {/*        type="number"*/}
            {/*        min="1"*/}
            {/*        value={userId}*/}
            {/*        onChange={(event) => setUserId(event.target.value)}*/}
            {/*    />*/}
            {/*</div>*/}

            <div>
                <label htmlFor="room">Room</label>

                <select
                    id="room"
                    value={roomId}
                    onChange={(event) => setRoomId(event.target.value)}
                >
                    <option value="">
                        Select a room
                    </option>

                    {rooms.map((room) => (
                        <option
                            key={room.id}
                            value={room.id}
                        >
                            {room.name} (Capacity: {room.capacity})
                        </option>
                    ))}
                </select>
            </div>

            <div>
                <label htmlFor="startTime">Start Time</label>
                <input
                    id="startTime"
                    type="datetime-local"
                    value={startTime}
                    onChange={(event) => setStartTime(event.target.value)}
                />
            </div>

            <div>
                <label htmlFor="endTime">End Time</label>
                <input
                    id="endTime"
                    type="datetime-local"
                    value={endTime}
                    onChange={(event) => setEndTime(event.target.value)}
                />
            </div>

            {error && <p>{error}</p>}

            {createMutation.isError && (<p>Failed to create booking:{" "} {createMutation.error.message}</p>)}

            {success && <p>{success}</p>}

            <button
                type="submit"
                disabled={createMutation.isPending}
                // disabled={submitting}
            >
                {createMutation.isPending ? "Booking..." : "Book Room"}
                {/*{submitting ? "Booking..." : "Book Room"}*/}
            </button>
        </form>
    );
}

export default BookingForm;

// OBS! Later, when we add authentication, the user will no longer type a userId manually at all;
// the backend will derive the logged-in user's identity from authentication.

// There are now also two kinds of error in this component:
//
// validationError
//     ↓
// React/client-side validation
// "End time must be after start time"
//
// createMutation.error
//     ↓
// Spring/backend/API failure
// "Room is already booked"
// "User not found..."