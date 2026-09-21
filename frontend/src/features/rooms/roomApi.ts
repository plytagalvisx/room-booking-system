import type { Room, CreateRoomRequest } from './roomTypes';
import { API_BASE_URL, authenticatedFetch } from '../../api/httpClient';
// import keycloak from "../../auth/keycloak"; // We will send the JWT from createRoom()


// getRooms() is public so keep fetch() as it is.
export async function getRooms(): Promise<Room[]> { // Promise<Room[]> means this asynchronous function will eventually return an array of Room. Because fetch() is asynchronous, it does not immediately return the rooms.
    const response = await fetch(`${API_BASE_URL}/rooms`);

    if (!response.ok) {
        throw new Error("Failed to fetch rooms");
    }

    const data: Room[] = await response.json(); // takes the JSON response body and converts it to a JavaScript value.
    return data;
}

export async function createRoom(room: CreateRoomRequest): Promise<Room> {

    // if (!keycloak.authenticated) {
    //     throw new Error("You must be logged in.");
    // }
    //
    // await keycloak.updateToken(30); // Refresh the token if it's about to expire in 30 seconds

    // const response = await fetch(`${API_BASE_URL}/rooms`,
    //     {
    //         method: "POST",
    //         headers: {
    //             "Content-Type": "application/json",
    //             Authorization: `Bearer ${keycloak.token}`, // Send the JWT in the Authorization header
    //         },
    //         body: JSON.stringify(room),
    //     }
    // );

    const response = await authenticatedFetch("/rooms", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(room),
    });

    if (response.ok) {
        return response.json();
    }

    if (response.status === 401) {
        throw new Error("You must be logged in.");
    }

    if (response.status === 403) {
        throw new Error("Administrator access required"); // You do not have permission to create a room.
    }

    throw new Error("Failed to create room"); // Something went wrong

}