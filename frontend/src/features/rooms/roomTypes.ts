export type Room = { // aka response DTO, or data transfer object, or data model
    id: number;
    name: string;
    capacity: number;
};

export type CreateRoomRequest = {
    name: string;
    capacity: number;
};


// That Java → JSON → TypeScript boundary is an important part of this project.


// Using interface
// export interface Room {
//     id: number;
//     name: string;
//     capacity: number;
// }

// There is no functional difference in how React, Vite, or TypeScript compiles or runs your application.
// Both will strictly enforce that your Room object has an id as a number, a name as a string, and a capacity as a number.

// However, choosing between a type alias and an interface introduces a few subtle,
// structural differences in how TypeScript handles them under the hood:
// - Re-declaring a type with the same name throws a duplicate identifier error, while re-declaring an interface merges the declarations.
// - Interfaces can be extended using the extends keyword, while type aliases can use intersection types (&) to achieve a similar effect. (e.g., `type ExtendedRoom = Room & { location: string }`) and (interface ExtendedRoom extends Room { location: string; })
// - Type aliases can represent more complex types (primitives), such as union types, tuples, or mapped types, which interfaces cannot directly express (they can strictly only describe object shapes).

// In modern React development (where functional components are standard), the common industry consensus is to default to type.
//
// 1. Perfect for Component Props: React component props frequently require union types
//    (e.g., type Status = 'loading' | 'success' | 'error'), which interface cannot handle.
//    Defaulting to type keeps your codebase highly consistent.
//
// 2. Safer for Application Data: You rarely want declaration merging for your internal data models like a Room.
//    If you accidentally define a second Room type elsewhere, a type alias will immediately throw an error to alert you,
//    whereas an interface will silently merge them and potentially mask bugs.
