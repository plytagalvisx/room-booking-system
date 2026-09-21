// This is more interesting than RoomCard because we'll simulate an actual user filling in the form and clicking buttons.
// We’ll start with client-side behavior only. We do not want these component tests to call our real Spring Boot backend.
// Because BookingForm uses TanStack Query's useMutation() and useQueryClient(), the test must also provide a QueryClientProvider.

// JSDOM is a powerful JavaScript library that simulates browser-like DOM environments inside Node.js.
// It allows us to test web code, parse HTML, and run browser APIs.

// Vitest
//      → test runner, similar role to JUnit
// React Testing Library
//      → renders React components and interacts with them
// jest-dom
//      → useful assertions like toBeInTheDocument()
// user-event
//      → simulates realistic user actions
// jsdom
//      → gives Vitest a fake browser DOM

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, beforeEach, vi, } from "vitest";

import BookingForm from "./BookingForm";
import { createBooking } from "./bookingApi";

const rooms = [
    {
        id: 1,
        name: "Conference Room A",
        capacity: 8,
    },
    {
        id: 2,
        name: "Meeting Room B",
        capacity: 4,
    },
];

function renderBookingForm() {
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
            <BookingForm rooms={rooms} />
        </QueryClientProvider>
    );
}

vi.mock("./bookingApi", () => ({
    createBooking: vi.fn(), // NOT the real Spring backend, instead mock function controlled by the test.
}));

const mockedCreateBooking = vi.mocked(createBooking);

describe("BookingForm", () => {
    beforeEach(() => {
        vi.clearAllMocks(); // or vi.resetAllMocks(); Reset the mock function before each test to avoid interference between tests.
    });

    // it("displays the booking form", () => {
    //     renderBookingForm(); // display the booking form on a virtual screen (jsdom).
    //     expect(screen.getByLabelText("User ID")).toBeInTheDocument();
    //     expect(screen.getByLabelText("Room")).toBeInTheDocument();
    //     expect(screen.getByLabelText("Start Time")).toBeInTheDocument();
    //     expect(screen.getByLabelText("End Time")).toBeInTheDocument();
    //     expect(screen.getByRole("button", { name: "Book Room", })).toBeInTheDocument();
    // });


    it("renders the booking form without a user ID field", () => {
        renderBookingForm();
        expect(screen.getByLabelText("Room")).toBeInTheDocument();
        expect(screen.getByLabelText("Start Time")).toBeInTheDocument();
        expect(screen.getByLabelText("End Time")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Book Room", })).toBeInTheDocument();
        expect(screen.queryByLabelText("User ID")).not.toBeInTheDocument();
    });

    it("shows validation error when fields are empty", async () => {
        renderBookingForm();
        const user = userEvent.setup();
        await user.click(screen.getByRole("button", { name: "Book Room", }));
        expect(screen.getByText("All fields are required.")).toBeInTheDocument();
        expect(mockedCreateBooking).not.toHaveBeenCalled();
    });

    // Now let's simulate filling the form (incorrectly):
    // We're using fireEvent.change() for the datetime-local fields because browser date/time inputs can be awkward to simulate with keyboard typing in jsdom.
    // This test proves the frontend catches the invalid date range before it ever attempts an HTTP request.
    it("shows validation error when end time is before start time", async () => {
        renderBookingForm();
        const user = userEvent.setup();
        // await user.type(screen.getByLabelText("User ID"), "1");
        await user.selectOptions(screen.getByLabelText("Room"), "2");

        fireEvent.change(screen.getByLabelText("Start Time"), { target: { value: "2030-01-10T12:00", }, }); // fireEvent is used here because userEvent.type() doesn't work well with datetime-local inputs in jsdom.
        fireEvent.change(screen.getByLabelText("End Time"), { target: { value: "2030-01-10T11:00", }, });
        // await user.type(screen.getByLabelText("Start Time"), "2030-01-10T11:00");
        // await user.type(screen.getByLabelText("End Time"), "2030-01-10T10:00");

        await user.click(screen.getByRole("button", {name: "Book Room",}));
        expect(screen.getByText("End time must be after start time.")).toBeInTheDocument();
        expect(mockedCreateBooking).not.toHaveBeenCalled();
    });

    // it("creates a booking when the form is valid", async () => { // aka shows a valid form when all fields are filled correctly
    //     renderBookingForm();
    //     const user = userEvent.setup();
    //     await user.type(screen.getByLabelText("User ID"), "1");
    //     await user.selectOptions(screen.getByLabelText("Room"), "1");
    //     fireEvent.change(screen.getByLabelText("Start Time"), { target: { value: "2030-01-10T12:00", }, });
    //     fireEvent.change(screen.getByLabelText("End Time"), { target: { value: "2030-01-10T13:00", }, });
    //     await user.click(screen.getByRole("button", { name: "Book Room", }));
    //
    //     // OBS! Here we would normally check for a successful submission message,
    //     // but since we haven't mocked the backend yet, we can't assert that.
    //
    //     // But now since we have mocked createBooking(), we can assert that it was called with the expected arguments:
    //     expect(createBooking).toHaveBeenCalledWith({
    //         userId: 1,
    //         roomId: 1,
    //         startTime: "2030-01-10T12:00",
    //         endTime: "2030-01-10T13:00",
    //     });
    // })

    it("creates a booking when the form is valid", async () => { // aka shows a valid form when all fields are filled correctly
        renderBookingForm();
        const user = userEvent.setup();
        // const mockedCreateBooking = vi.mocked(createBooking);

        mockedCreateBooking.mockResolvedValue({ // here we define what the mock function should return when called
            id: 42,
            userId: 17,
            roomId: 2,
            startTime: "2030-01-10T10:00",
            endTime: "2030-01-10T11:00",
        });

        // await user.type(screen.getByLabelText("User ID"), "1");
        await user.selectOptions(screen.getByLabelText("Room"), "2");
        fireEvent.change(screen.getByLabelText("Start Time"), { target: { value: "2030-01-10T10:00", }, });
        fireEvent.change(screen.getByLabelText("End Time"), { target: { value: "2030-01-10T11:00", }, });
        await user.click(screen.getByRole("button", { name: "Book Room", }));

        expect(mockedCreateBooking).toHaveBeenCalledTimes(1);

        expect(mockedCreateBooking.mock.calls[0][0]).toEqual({
            // userId: 1,
            roomId: 2,
            startTime: "2030-01-10T10:00",
            endTime: "2030-01-10T11:00",
        });

        // Alternative:
        // expect(mockedCreateBooking).toHaveBeenCalledWith({
        //         // userId: 1,
        //         roomId: 2,
        //         startTime: "2030-01-10T10:00",
        //         endTime: "2030-01-10T11:00",
        //     },
        //     expect.anything()
        // );

        expect(await screen.findByText("Booking 42 created successfully.")).toBeInTheDocument(); // wait for the success message to appear in the DOM
    });

    // backend-error test (API failure error display test):
    // Let's also prove the form displays an API error, such as our 409 Conflict when calling a createBooking API.
    it("shows an API error when booking creation fails", async () => {
        renderBookingForm();
        const user = userEvent.setup();
        // const mockedCreateBooking = vi.mocked(createBooking);

        mockedCreateBooking.mockRejectedValue(
            new Error("Room is already booked")
        );

        // await user.type(screen.getByLabelText("User ID"), "1");
        await user.selectOptions(screen.getByLabelText("Room"), "1");
        fireEvent.change(screen.getByLabelText("Start Time"), { target: { value: "2030-01-10T10:00", }, });
        fireEvent.change(screen.getByLabelText("End Time"), { target: { value: "2030-01-10T11:00", }, });
        await user.click(screen.getByRole("button", { name: "Book Room", }));

        expect(await screen.findByText(/Room is already booked/)).toBeInTheDocument();
    });

});

// RoomCard
//   ✓ displays the room name and capacity
//
// BookingForm
//   ✓ displays the booking form
//   ✓ shows an error when fields are empty
//   ✓ shows an error when end time is before start time

// RoomCard
// → simple rendering test
//
// BookingForm
// → user interaction + state + validation test

// The next test after these pass will be the really useful one: we'll mock createBooking() similarly to
// how you used Mockito on the backend, submit a valid form, and verify that BookingForm actually sends
// the expected BookingRequest and displays the successful response.

// We use await screen.findByText() instead of screen.getByText() because
// the mutation is asynchronous:
//
// click
//  ↓
// createMutation.mutate()
//  ↓
// Promise
//  ↓
// mock resolves
//  ↓
// onSuccess()
//  ↓
// setSuccess()
//  ↓
// React rerenders
//
// findByText() waits for the element to appear.


// The dependency being mocked is different:
//
// Backend unit test
// BookingService
//     ↓
// mock BookingRepository
//
// Frontend component test
// BookingForm
//     ↓
// mock bookingApi.createBooking
//
// That is the same testing principle: isolate the unit/component from the next external layer.


// OBS!
// Also remember why we're using:
//     mockedCreateBooking.mock.calls[0][0]
//
// instead of simply:
//     expect(mockedCreateBooking).toHaveBeenCalledWith(...)
//
// TanStack Query may pass an additional internal mutation context argument.
// We only care about the first argument—our application's BookingRequest.