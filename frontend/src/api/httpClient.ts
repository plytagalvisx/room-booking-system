// API Client: This is a wrapper around the fetch API that adds the Authorization header with the Keycloak access token.

import keycloak from "../auth/keycloak";
import { env } from "../config/env";

export const API_BASE_URL= env.apiBaseUrl; // import.meta.env.VITE_API_BASE_URL;

// We want every authenticated API call to use one reusable request function:
export async function authenticatedFetch(path: string, options: RequestInit = {}): Promise<Response> {

    if (!keycloak.authenticated) {
        throw new Error("You must be logged in.");
    }

    await keycloak.updateToken(30);

    if (!keycloak.token) {
        throw new Error("No access token is available.");
    }

    const headers = new Headers(options.headers);

    headers.set(
        "Authorization", `Bearer ${keycloak.token}`
    );

    return fetch(`${API_BASE_URL}${path}`,
        {
            ...options,
            headers,
        }
    );
}

// path means callers can simply write:
// authenticatedFetch("/bookings/me")

// And const headers = new Headers(options.headers);
// preserves headers supplied by the caller.

// For example, booking creation supplies:
//
//     "Content-Type": "application/json"
//
// Then our wrapper adds:
//
//     Authorization: Bearer ...
//
// giving us:
//
//     Content-Type: application/json
//     Authorization: Bearer eyJ...
//
// We don't overwrite one with the other.