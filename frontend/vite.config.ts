import react from '@vitejs/plugin-react'
// import { defineConfig } from 'vite'
import { defineConfig } from "vitest/config";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom", // Run the tests with a simulated (i.e., fake) browser DOM. Without it, Node doesn't naturally understand things such as: document, window, button, and input.
    setupFiles: "./src/test/setup.ts",

    exclude: [
      "node_modules/**",
      "e2e/**",
    ],
  },
})

// We have a clear separation:
// Vitest:
// → src/**/*.test.tsx
// → component/unit tests
//
// Playwright:
// → e2e/**/*.spec.ts
// → browser E2E tests



// OBS!
// So the testing architecture becomes:
// npm test
//    ↓
// Vitest
//    ↓
// RoomCard.test.tsx
// BookingForm.test.tsx
// MyBookingsPage.test.tsx
// ...
//
//
// npm run test:e2e
//    ↓
// Playwright
//    ↓
// e2e/booking.spec.ts
//    ↓
// React → Keycloak → Spring → PostgreSQL