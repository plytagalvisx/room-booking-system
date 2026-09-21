import { useState } from "react";
import type { FormEvent } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createRoom, } from "../rooms/roomApi";
import { hasRealmRole } from "../auth/roles";

function AdminPage() {
    const [roomName, setRoomName] = useState("");
    const [capacity, setCapacity] = useState("");
    const [validationError, setValidationError] = useState<string | null>(null);

    const queryClient = useQueryClient();

    const createRoomMutation = useMutation({
        mutationFn: createRoom,
        onSuccess: () => {
            // here we can reset the form fields after a successful room creation
            setRoomName("");
            setCapacity("");

            // When this is executed, TanStack Query knows the rooms cache is stale (meaning it needs to be updated), and the new room should appear when we visit /rooms route link.
            queryClient.invalidateQueries({ // here we invalidate the query for rooms so that it will refetch the updated list of rooms after a new room is created.
                queryKey: ["rooms"]
            });
        },
    });

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setValidationError(null);
        createRoomMutation.reset();

        if (!roomName || !capacity) {
            setValidationError("All fields are required.");
            return;
        }

        const numericCapacity = Number(capacity);

        if (!Number.isInteger(numericCapacity) || numericCapacity <= 0) {
            setValidationError("Capacity must be a positive integer.");
            return;
        }

        createRoomMutation.mutate({
            name: roomName,
            capacity: numericCapacity,
        });
    };

    if (!hasRealmRole("ADMIN")) { // security layer --> improves the user experience. The backend provides the actual security by: .hasRole("ADMIN")
        return (
            <main>
                <h1>Forbidden</h1>
                <p>
                    Administrator access required.
                </p>
            </main>
        );
    }

    return (
        <main>
            <h1>Admin Page</h1>

            <h2>Create Room</h2>

            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="roomName">
                        Room name
                    </label>

                    <input
                        id="roomName"
                        value={roomName}
                        onChange={(event) => setRoomName(event.target.value)}
                        placeholder="Enter room name"
                    />
                </div>

                <div>
                    <label htmlFor="capacity">
                        Capacity
                    </label>

                    <input
                        id="capacity"
                        type="number"
                        value={capacity}
                        onChange={(event) => setCapacity(event.target.value)}
                        placeholder="Enter capacity"
                    />
                </div>

                <button
                    type="submit"
                    disabled={createRoomMutation.isPending}
                >
                    {createRoomMutation.isPending ? "Creating..." : "Create Room"}
                </button>
            </form>

            {validationError && (<p>{validationError}</p>)}

            {createRoomMutation.isError && (<p>{createRoomMutation.error.message}</p>)}

            {createRoomMutation.isSuccess && (<p>Room created successfully.</p>)}

        </main>
    );
}

export default AdminPage;