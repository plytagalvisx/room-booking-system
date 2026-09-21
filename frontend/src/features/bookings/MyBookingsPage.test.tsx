import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor, } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, beforeEach, vi, } from "vitest";

import MyBookingsPage from "./MyBookingsPage";
import { getMyBookings, deleteBooking } from "./bookingApi";

function renderMyBookingsPage() {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false,
            },
            mutations: {
                retry: false,
            },
        },
    });

    render(
        <QueryClientProvider client={queryClient}>
            <MyBookingsPage />
        </QueryClientProvider>
    );
}

vi.mock("./bookingApi", () => ({
    getMyBookings: vi.fn(), // NOT the real Spring backend, instead it's a mock function controlled by the test.
    deleteBooking: vi.fn(),
}));

const mockedGetMyBookings = vi.mocked(getMyBookings);
const mockedDeleteBooking = vi.mocked(deleteBooking);

const booking = {
    id: 42,
    userId: 17,
    roomId: 2,
    startTime: "2030-01-10T10:00:00",
    endTime: "2030-01-10T11:00:00",
};

// MyBookingsPage combines both sides of TanStack Query at once: we'll simulate entering a user ID,
// loading bookings with useQuery, then clicking Cancel booking, mocking deleteBooking(),
// and verifying query invalidation/refetch behavior.
// useQuery()       → load bookings
// useMutation()    → delete booking
// invalidation     → refetch bookings
describe("MyBookingsPage", () => {
    beforeEach(() => {
        vi.clearAllMocks(); // Reset the mock function before each test to avoid interference between tests.
    });

    it("loads and displays bookings automatically", async () => {
        // const user = userEvent.setup();

        // const mockedGetBookingsForUser = vi.mocked(getBookingsForUser);
        mockedGetMyBookings.mockResolvedValue([
            booking,
            // { id: 10, userId: 5, roomId: 2, startTime: "2030-01-10T10:00:00", endTime: "2030-01-10T11:00:00" },
        ]);

        renderMyBookingsPage();

        // await user.type(screen.getByLabelText("User ID"), "5");
        // await user.click(screen.getByRole("button", { name: "Load Bookings" }));

        expect(await screen.findByText("Booking #42")).toBeInTheDocument();
        expect(screen.getByText("Room ID: 2")).toBeInTheDocument();
        expect(screen.getByText("Start time: 2030-01-10T10:00:00")).toBeInTheDocument();
        expect(screen.getByText("End time: 2030-01-10T11:00:00")).toBeInTheDocument();

        // Verify that getBookingsForUser() was called with the correct user ID
        // expect(mockedGetBookingsForUser).toHaveBeenCalledWith(5);
        // Or:
        // expect(mockedGetBookingsForUser.mock.calls[0][0]).toBe(5);

        expect(mockedGetMyBookings).toHaveBeenCalledTimes(1);
    });

    it("shows an empty state", async () => {
        // const user = userEvent.setup();

        // const mockedGetBookingsForUser = vi.mocked(getBookingsForUser);

        mockedGetMyBookings.mockResolvedValue([]);

        renderMyBookingsPage();

        // await user.type(screen.getByLabelText("User ID"), "5");
        // await user.click(screen.getByRole("button", { name: "Load Bookings" }));

        expect(await screen.findByText("No bookings found.")).toBeInTheDocument();

        // expect(mockedGetBookingsForUser).toHaveBeenCalledWith(5);
    })

    it("shows an error when bookings cannot be loaded", async () => {
        // const user = userEvent.setup();

        // const mockedGetBookingsForUser = vi.mocked(getBookingsForUser);

        mockedGetMyBookings.mockRejectedValue(
            new Error("Failed to fetch bookings")
        );

        renderMyBookingsPage();

        // await user.type(screen.getByLabelText("User ID"), "5");
        // await user.click(screen.getByRole("button", { name: "Load Bookings" }));

        expect(await screen.findByText(/Failed to fetch bookings/)).toBeInTheDocument();

        // expect(mockedGetBookingsForUser).toHaveBeenCalledWith(5);

        // getBookingsForUser()
        //  ↓ rejects
        //         TanStack Query
        //  ↓
        // isError = true
        //  ↓
        // error.message displayed
    })

    // it("shows an error when user is not found", async () => {
    //     const user = userEvent.setup();
    //
    //     const mockedGetBookingsForUser = vi.mocked(getBookingsForUser);
    //
    //     mockedGetBookingsForUser.mockRejectedValue(
    //         new Error("User not found")
    //     );
    //
    //     renderMyBookingsPage();
    //
    //     await user.type(screen.getByLabelText("User ID"), "999");
    //     await user.click(screen.getByRole("button", { name: "Load Bookings" }));
    //
    //     expect(await screen.findByText(/User not found/)).toBeInTheDocument();
    //
    //     expect(mockedGetBookingsForUser).toHaveBeenCalledWith(999);
    // })
    //
    // it("does not load bookings before a user is selected", async () => {
    //     const mockedGetBookingsForUser = vi.mocked(getBookingsForUser);
    //
    //     renderMyBookingsPage();
    //
    //     expect(mockedGetBookingsForUser).not.toHaveBeenCalled();
    // })

    // Here we're testing the complete TanStack Query synchronization behavior.
    it("cancels a booking and reloads the booking list", async () => {
        const user = userEvent.setup();

        // const mockedGetBookingsForUser = vi.mocked(getBookingsForUser);
        // const mockedDeleteBooking = vi.mocked(deleteBooking);

        // This line is particularly important: mockedGetBookings.mockResolvedValueOnce([booking]).mockResolvedValueOnce([]);
        // This is basically our ground truth that we need to obtain:
        mockedGetMyBookings.mockResolvedValueOnce([
            booking,
            // { id: 10, userId: 5, roomId: 2, startTime: "2030-01-10T10:00:00", endTime: "2030-01-10T11:00:00" },
        ]).mockResolvedValueOnce([]); // After deletion, return an empty list (reloads the bookings list)

        mockedDeleteBooking.mockResolvedValue(undefined);

        renderMyBookingsPage();

        // await user.type(screen.getByLabelText("User ID"), "5");
        // await user.click(screen.getByRole("button", { name: "Load Bookings" }));

        expect(await screen.findByText("Booking #42")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "Cancel Booking" }));

        await waitFor(() => {
            expect(mockedDeleteBooking).toHaveBeenCalledTimes(1);
            expect(mockedGetMyBookings).toHaveBeenCalledTimes(2); // Called again after deletion
        });

        expect(mockedDeleteBooking.mock.calls[0][0]).toBe(42);
        expect(await screen.findByText("No bookings found.")).toBeInTheDocument();

        // First GET
        // → booking exists
        //
        // DELETE succeeds
        //
        // Second GET after invalidation
        // → booking no longer exists
    });

    it("shows an error when cancellation fails", async () => {
        const user = userEvent.setup();

        // const mockedGetBookingsForUser = vi.mocked(getBookingsForUser);
        // const mockedDeleteBooking = vi.mocked(deleteBooking);

        mockedGetMyBookings.mockResolvedValue([
            booking,
            // { id: 10, userId: 5, roomId: 2, startTime: "2030-01-10T10:00:00", endTime: "2030-01-10T11:00:00" },
        ]);

        mockedDeleteBooking.mockRejectedValue(
            new Error("Failed to cancel booking.")
        );

        renderMyBookingsPage();

        // await user.type(screen.getByLabelText("User ID"), "5");
        // await user.click(screen.getByRole("button", { name: "Load Bookings" }));

        await screen.findByText("Booking #42");
        await user.click(screen.getByRole("button", { name: "Cancel Booking" }));

        expect(await screen.findByText(/Failed to cancel booking/)).toBeInTheDocument();
        expect(screen.getByText("Booking #42")).toBeInTheDocument();
    });
});

// RoomCard
//   ✓ rendering
//
// BookingForm
//   ✓ rendering
//   ✓ required validation
//   ✓ invalid dates
//   ✓ successful POST mutation
//   ✓ failed POST mutation
//
// RoomListPage
//   ✓ loading
//   ✓ rooms returned
//   ✓ empty []
//   ✓ GET error
//
// MyBookingsPage
//   ✓ doesn't query initially
//   ✓ loads bookings
//   ✓ empty bookings
//   ✓ GET error
//   ✓ DELETE success + query refetch
//   ✓ DELETE error