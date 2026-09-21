import { Navigate, NavLink, Route, Routes } from "react-router";

import RoomListPage from "./features/rooms/RoomListPage";
import BookingPage from "./features/bookings/BookingPage";
import MyBookingsPage from "./features/bookings/MyBookingsPage";
import AuthPage from "./features/auth/AuthPage";
import AdminPage from "./features/admin/AdminPage.tsx";
import { hasRealmRole } from "./features/auth/roles";

function App() {
  return (
      <>
        <header>
          <h1>Room Booking</h1>
          <nav>
            <NavLink to="/rooms" style={({ isActive }) => ({ fontWeight: isActive ? 'bold' : 'normal' })}>Rooms</NavLink>
            {" | "}
            <NavLink to="/book" style={({ isActive }) => ({ fontWeight: isActive ? 'bold' : 'normal' })}>Book a room</NavLink>
            {" | "}
            <NavLink to="/bookings" style={({ isActive }) => ({ fontWeight: isActive ? 'bold' : 'normal' })}>My Bookings</NavLink>
            {" | "}
            <NavLink to="/auth" style={({ isActive }) => ({ fontWeight: isActive ? 'bold' : 'normal' })}>Account</NavLink>
            {" | "}
            {hasRealmRole("ADMIN") && (
                <NavLink to="/admin" style={({ isActive }) => ({ fontWeight: isActive ? 'bold' : 'normal' })}>Admin</NavLink>
            )}
          </nav>
        </header>

        <Routes>
          <Route path="/" element={<Navigate to="/rooms" replace />} />
          <Route path="/rooms" element={<RoomListPage />} />
          <Route path="/book" element={<BookingPage />} />
          <Route path="/bookings" element={<MyBookingsPage />} />
          <Route path="/auth" element={<AuthPage />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="*" element={<h2>Page not found</h2>} />
        </Routes>
      </>
  );
}

export default App;



