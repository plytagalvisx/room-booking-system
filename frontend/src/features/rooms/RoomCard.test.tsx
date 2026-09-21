// This is a component test for RoomCard.

import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import RoomCard from "./RoomCard";

const room = {
    id: 1,
    name: "Conference Room A",
    capacity: 8,
};

describe("RoomCard", () => {
    it("displays the room name and capacity", () => {
        render(<RoomCard room={room} />);
        expect(screen.getByText("Conference Room A")).toBeInTheDocument();
        expect(screen.getByText("Capacity: 8")).toBeInTheDocument();
    });
});


// So this render() call:
//
//     render(<RoomCard room={room} />);
//
// doesn't open Chrome. React Testing Library creates a DOM in jsdom:
//
//     <div>
//         <h2>Conference Room A</h2>
//         <p>Capacity: 8</p>
//     </div>
//
// Then this:
//
//     screen.getByText("Conference Room A")
//
// searches what the user could see.
//
// And then:
//
//     expect(...).toBeInTheDocument();
//
// checks whether it exists.
//
// So:
//
//     Room object
//         ↓
//     render RoomCard
//         ↓
//     React produces DOM
//         ↓
//     Testing Library searches DOM
//         ↓
//     assert expected UI


// We don't want tests like:
//
// Does RoomCard call internal function X?
//     Does it have exactly three divs?
//     Does some private state contain value Y?
//
// We care more about:
//
//     Given this room, what does the user see?
//
// That's a major philosophy of React Testing Library.
//
// Your first test therefore says:
//
//     Given:
//         Room {
//     name = Conference Room A
//     capacity = 8
// }
//
// When:
//     RoomCard renders
//
// Then:
//     user sees "Conference Room A"
//     and "Capacity: 8"
//
// Very similar to how your backend tests expressed behavior rather than simply testing getters/setters.