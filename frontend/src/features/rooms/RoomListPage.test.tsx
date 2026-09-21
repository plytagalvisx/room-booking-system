import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, } from "@testing-library/react";
import { describe, expect, it, beforeEach, vi, } from "vitest";

import RoomListPage from "./RoomListPage.tsx";
import { getRooms } from "./roomApi";

function renderRoomListPage() {
    const queryClient = new QueryClient({ // we use this because the RoomListPage component uses TanStack Query to fetch data from the backend. We need to provide a QueryClient instance to the component for it to work properly.
        defaultOptions: {
            queries: { // we use only useQuery() in this page/component
                retry: false,
            },
            // mutations: { // This page/component doesn't use useMutation(), so we don't need to set retry: false for them.
            //     retry: false,
            // },
        },
    });

    render(
        <QueryClientProvider client={queryClient}>
            <RoomListPage />
        </QueryClientProvider>
    );
}

vi.mock("./roomApi", () => ({
    getRooms: vi.fn(), // NOT the real Spring backend, instead mock function controlled by the test.
}));


// test RoomListPage with TanStack Query and mocked API responses — including loading, successful data, empty results, and errors
describe("RoomListPage", () => {
    beforeEach(() => {
        vi.clearAllMocks(); // Reset the mock function before each test to avoid interference between tests.
    });

    // Test successful room loading and Verify that getRooms() was actually called
    it("display rooms returned by the API", async () => {
        const mockedGetRooms = vi.mocked(getRooms);

        mockedGetRooms.mockResolvedValue([ // here we define what the mock getRooms() function should return when called
            { id: 1, name: "Conference Room A", capacity: 8 },
            { id: 2, name: "Meeting Room B", capacity: 4 },
        ]);

        renderRoomListPage(); // Render AFTER the mock has been configured because the component will call getRooms() immediately on mount.
        // OBS! Each individual test should render the component after establishing what its API mock should return.

        // Now make assertions:
        expect(await screen.findByText("Conference Room A")).toBeInTheDocument(); // this query here resolves asynchronously
        expect(screen.getByText("Capacity: 8")).toBeInTheDocument();
        expect(screen.getByText("Meeting Room B")).toBeInTheDocument();
        expect(screen.getByText("Capacity: 4")).toBeInTheDocument();

        // We can make this test slightly stronger: Verify  that getRooms() was actually called
        expect(mockedGetRooms).toHaveBeenCalledTimes(1); // Verify that getRooms() was called exactly once.

        // So here we're testing two things:
        // 1. Query function executed
        // 2. Result rendered
    });

    // Test empty database
    it("shows a message when there are no rooms", async () => {
        const mockedGetRooms = vi.mocked(getRooms);

        mockedGetRooms.mockResolvedValue([]);

        renderRoomListPage();

        expect(await screen.findByText("No rooms found.")).toBeInTheDocument();
        // That's important: an empty result and an API failure are two different states.
    });

    // Test API failure
    it("shows an error when rooms cannot be loaded", async () => {
        const mockedGetRooms = vi.mocked(getRooms);

        mockedGetRooms.mockRejectedValue(
            new Error("Failed to fetch rooms")
        );

        renderRoomListPage();

        expect(await screen.findByText("Error: Failed to fetch rooms")).toBeInTheDocument();

        // This represents something like:
        // GET /api/rooms
        //       ↓
        // 500 / network problem
        //       ↓
        // getRooms throws Error
        //       ↓
        // TanStack Query
        //         isError = true
        //       ↓
        // RoomListPage displays error
    });

    // Test the loading state
    it("shows loading while rooms are being fetched", async () => {
        const mockedGetRooms = vi.mocked(getRooms);

        mockedGetRooms.mockImplementation(() =>
            new Promise(() => {})
        );
        // never resolves, simulating a loading state. We create a promise that remains pending.
        // So TanStack Query stays at: isLoading = true, isError = false, data = undefined

        renderRoomListPage();

        expect(screen.getByText("Loading rooms...")).toBeInTheDocument();
    });


});


// Again we're replacing:
//
// getRooms()
//    ↓
// real fetch()
//    ↓
// Spring
//
//
// with:
//
// getRooms()
//    ↓
// Vitest mock
//
// So Spring Boot does not need to be running for these tests.


// At this point your frontend tests should cover roughly:
// RoomCard
//   ✓ rendering
//
// BookingForm
//   ✓ rendering
//   ✓ required validation
//   ✓ invalid time validation
//   ✓ successful mutation
//   ✓ failed mutation
//
// RoomListPage
//   ✓ loading
//   ✓ successful query
//   ✓ empty query result
//   ✓ failed query