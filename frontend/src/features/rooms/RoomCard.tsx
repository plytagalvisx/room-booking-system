import type { Room } from "./roomTypes";

type RoomCardProps = { // RoomCard requires a property called room, and it must match our Room type.
    room: Room;
};

function RoomCard({ room }: RoomCardProps) { // This extracts that room prop.
    return (
        <div>
            <h2>{room.name}</h2>
            <p>Capacity: {room.capacity}</p>
        </div>
    );
}

export default RoomCard;

// Conceptually:
//
//     RoomListPage
//          │
//          ├── RoomCard(room A)
//          ├── RoomCard(room B)
//          └── RoomCard(room C)

// This parent → child data flow through props is one of the React concepts we specifically wanted to refresh.