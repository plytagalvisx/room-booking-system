import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider, } from "@tanstack/react-query";
import { beforeEach, describe, expect, it, vi, } from "vitest";

import AdminPage from "./AdminPage";
import { createRoom } from "../rooms/roomApi";
import { hasRealmRole } from "../auth/roles";

// We want to test the two important cases:
// USER
// → sees Forbidden
// → cannot use create-room form
//
// ADMIN
// → sees create-room form
// → can submit room
// → createRoom() is called
// → success message appears

function renderAdminPage() {
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
            <AdminPage />
        </QueryClientProvider>
    );

    return queryClient;
}

vi.mock("../rooms/roomApi", () => ({
    createRoom: vi.fn(),
}));

vi.mock("../auth/roles", () => ({
    hasRealmRole: vi.fn(),
}));

const mockedCreateRoom = vi.mocked(createRoom);
const mockedHasRealmRole = vi.mocked(hasRealmRole);


describe("AdminPage", () => {
    beforeEach(() => {
        vi.resetAllMocks();
    });

    it("shows Forbidden for a non-admin user", () => {
        mockedHasRealmRole.mockReturnValue(false); // Simulate a non-admin user

        renderAdminPage();

        expect(screen.getByRole("heading", {name: "Forbidden",})).toBeInTheDocument();
        expect(screen.getByText("Administrator access required.")).toBeInTheDocument();
        expect(screen.queryByRole("button", {name: "Create Room",})).not.toBeInTheDocument();
    });

    it("shows the create-room form for an admin", () => {
        mockedHasRealmRole.mockReturnValue(true);

        renderAdminPage();

        expect(screen.getByRole("heading", { name: "Admin Page", })).toBeInTheDocument();
        expect(screen.getByLabelText("Room name")).toBeInTheDocument();
        expect(screen.getByLabelText("Capacity")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Create Room", })).toBeInTheDocument();
    });

    it("shows validation error when fields are empty", async () => {
        const user = userEvent.setup();

        mockedHasRealmRole.mockReturnValue(true);

        renderAdminPage();

        await user.click(screen.getByRole("button", { name: "Create Room", }));

        expect(screen.getByText("All fields are required.")).toBeInTheDocument();
        expect(mockedCreateRoom).not.toHaveBeenCalled();
    });

    it("rejects an invalid capacity", async () => {
        const user = userEvent.setup();

        mockedHasRealmRole.mockReturnValue(true);

        renderAdminPage();

        await user.type(screen.getByLabelText("Room name"), "Conference Room");
        await user.type(screen.getByLabelText("Capacity"), "0");
        await user.click(screen.getByRole("button", { name: "Create Room", }));

        expect(screen.getByText("Capacity must be a positive integer.")).toBeInTheDocument();
        expect(mockedCreateRoom).not.toHaveBeenCalled();
    });

    it("creates a room successfully", async () => {
        const user = userEvent.setup();

        mockedHasRealmRole.mockReturnValue(true);

        mockedCreateRoom.mockResolvedValue({
            id: 10,
            name: "Conference Room",
            capacity: 8,
        });

        renderAdminPage();

        await user.type(screen.getByLabelText("Room name"), "Conference Room");
        await user.type(screen.getByLabelText("Capacity"), "8");
        await user.click(screen.getByRole("button", { name: "Create Room", }));

        expect(mockedCreateRoom).toHaveBeenCalledTimes(1);
        expect(mockedCreateRoom.mock.calls[0][0]).toEqual({
            name: "Conference Room",
            capacity: 8,
        });
        expect(await screen.findByText("Room created successfully.")).toBeInTheDocument();
    });

    it("shows an error when room creation fails", async () => {
        const user = userEvent.setup();

        mockedHasRealmRole.mockReturnValue(true);

        mockedCreateRoom.mockRejectedValue(
            new Error("Failed to create room.")
        );

        renderAdminPage();

        await user.type(screen.getByLabelText("Room name"), "Conference Room");
        await user.type(screen.getByLabelText("Capacity"), "8");
        await user.click(screen.getByRole("button", { name: "Create Room", }));

        expect(await screen.findByText("Failed to create room.")).toBeInTheDocument();
    });
});