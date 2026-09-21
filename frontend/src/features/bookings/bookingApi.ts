import type { BookingRequest, BookingResponse } from "./bookingTypes.ts";
// import { API_BASE_URL } from "../../api/httpClient.ts";
// import keycloak from "../../auth/keycloak"; // We will send the JWT from createBooking()
import { authenticatedFetch } from "../../api/httpClient";

export async function createBooking(booking: BookingRequest): Promise<BookingResponse> {

    // if (!keycloak.authenticated) {
    //     throw new Error("You must be logged in to create a booking.");
    // }
    //
    // await keycloak.updateToken(30); // Refresh the token if it's about to expire in 30 seconds

    // const response = await fetch(`${API_BASE_URL}/bookings`,
    //     {
    //         method: "POST",
    //         headers: {
    //             "Content-Type": "application/json",
    //             Authorization: `Bearer ${keycloak.token}`, // Send the JWT in the Authorization header
    //         },
    //         body: JSON.stringify(booking),
    //     }
    // );

    // createBooking() doesn't need to understand Keycloak anymore.
    // Its responsibility is simply:
    // POST a booking.
    const response = await authenticatedFetch("/bookings", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(booking),
    });

    if (response.ok) {
        return response.json();
    }

    const errorData = await response.json();

    if (response.status === 400) {
        throw new Error(errorData.detail ?? "Invalid booking request"); // The nullish coalescing operator (??) is used to provide a default error message in case the detail property is undefined or null.
    }

    if (response.status === 401) {
        throw new Error("You must be logged in.");
    }

    if (response.status === 404) {
        throw new Error(errorData.detail ?? "User or room not found");
    }

    if (response.status === 409) {
        throw new Error(errorData.detail ?? "Room is already booked");
    }

    throw new Error("Failed to create booking"); // Something went wrong
}

// export async function getBookingsForUser(userId: number): Promise<BookingResponse[]> {
export async function getMyBookings(): Promise<BookingResponse[]> {
    // if(!keycloak.authenticated) {
    //     throw new Error("You must be logged in.");
    // }
    //
    // await keycloak.updateToken(30); // Refresh the token if it's about to expire in 30 seconds

    // const response = await fetch(`${API_BASE_URL}/bookings/me`, // /user/${userId}`,
    //     {
    //         headers: {
    //             Authorization: `Bearer ${keycloak.token}`, // Send the JWT in the Authorization header
    //         },
    //     }
    // );

    const response = await authenticatedFetch("/bookings/me");

    if (!response.ok) {
        if (response.status === 401) {
            throw new Error("You must be logged in.");
        }

        if (response.status === 404) {
            throw new Error("User not found");
        }

        throw new Error("Failed to fetch bookings");
    }

    return response.json();
}

export async function deleteBooking(bookingId: number): Promise<void> {

    // if(!keycloak.authenticated) {
    //     throw new Error("You must be logged in.");
    // }
    //
    // await keycloak.updateToken(30); // Refresh the token if it's about to expire in 30 seconds

    // const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}`, {
    //     method: "DELETE",
    //     headers: {
    //         Authorization: `Bearer ${keycloak.token}`, // Send the JWT in the Authorization header
    //     },
    // });

    const response = await authenticatedFetch(`/bookings/${bookingId}`, {
        method: "DELETE",
    });

    if (response.ok) {
        return;
    }

    if (response.status === 401) {
        throw new Error("You must be logged in.");
    }

    if (response.status === 403) {
        throw new Error("You are not allowed to cancel this booking.");
    }

    if (response.status === 404) {
        throw new Error("Booking not found");
    }

    throw new Error("Failed to cancel booking");
}