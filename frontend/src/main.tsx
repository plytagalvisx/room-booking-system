import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { BrowserRouter } from "react-router";
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import keycloak from "./auth/keycloak";

const queryClient = new QueryClient();

async function startApplication() {
    try {
        const authenticated = await keycloak.init({
            onLoad: 'check-sso', // Checks whether this browser already has a Keycloak login session. If it does, authenticate the React application. If not, leave the user unauthenticated. The alternative would be: onLoad: "login-required", which means: Nobody may even load the React application until they log in.
            pkceMethod: "S256",
        });

        console.log("Keycloak authenticated: ", authenticated);

        createRoot(document.getElementById('root')!).render(
            <StrictMode>
                <QueryClientProvider client={queryClient}>
                    <BrowserRouter>
                        <App />
                    </BrowserRouter>
                </QueryClientProvider>
            </StrictMode>,
        )
    } catch (error) {
        console.error("Failed to initialize Keycloak:", error);
    }
}

startApplication();

// OBS! Server state and caching (server-state caching):
// TanStack Query (formerly known as React Query) is an open-source library used
// for fetching, caching, synchronizing, and updating server state in web applications.

// We use QueryClientProvider for our entire React application to provide a QueryClient instance that manages server state and caching.
// We use useQueryClient() to access the QueryClient instance for invalidating queries and refetching data when server state changes.
// We use useQuery() for fetching data from the server and caching it.
// We use useMutation() for changing server state (like creating, updating, or deleting data) and invalidating relevant queries to refetch updated data.

// Unlike client state (like UI toggles or form inputs), server state is data that lives
// on a remote server (like a database) and must be fetched asynchronously via APIs.
// TanStack Query acts as a smart middleman that eliminates the need for writing complex,
// repetitive boilerplate code like manual useEffect hooks and multiple useState declarations.

// This is considered manual code:
// loading
// error
// useEffect
// setRooms
// setBookings
// manual refetching

// which TanStack Query will replace.


// QueryClientProvider
//         ↓
// stores/manages server state
//         ↓
// my entire React application


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

// OBS!
// Fetching data uses:
// useQuery()
//
// Changing server state uses:
// useMutation()