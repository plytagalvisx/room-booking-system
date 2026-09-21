import { expect, test, } from "@playwright/test";

const username = process.env.E2E_USERNAME ?? "";
const password = process.env.E2E_PASSWORD ?? "";

function toDateTimeLocal(date: Date): string {
    const pad = (value: number) => String(value).padStart(2, "0");

    return (
        `${date.getFullYear()}-` +
        `${pad(date.getMonth() + 1)}-` +
        `${pad(date.getDate())}T` +
        `${pad(date.getHours())}:` +
        `${pad(date.getMinutes())}`
    );
}


// Users are no longer invented directly by the test.
// The identity now comes from:
// Keycloak
// → JWT
// → users.keycloak_subject
test("authenticated user can create, view and cancel a booking", async ({ page }) => {

        test.skip(!username || !password, "E2E_USERNAME and E2E_PASSWORD are required");

        // ---------------------------------
        // 1. Login through Keycloak
        // ---------------------------------

        await page.goto("/auth");

        await page.getByRole("button", { name: "Login", }).click();

        await expect(page).toHaveURL(/localhost:8081/);

        await page.locator("#username").fill(username);
        await page.locator("#password").fill(password);
        await page.locator("#kc-login").click();

        await expect(page).toHaveURL(/localhost:5173\/auth/);
        await expect(page.getByText(`Authenticated as: ${username}`)).toBeVisible();

        // ---------------------------------
        // 2. Create booking
        // ---------------------------------

        await page.goto("/book");
        const roomSelect = page.getByLabel("Room");


        await expect(roomSelect).toBeVisible();

        const firstRoomOption = roomSelect.locator('option:not([value=""])').first();
        await expect(firstRoomOption).toBeAttached();

        const roomValue = await firstRoomOption.getAttribute("value");
        expect(roomValue).not.toBeNull();

        await roomSelect.selectOption(roomValue!);


        // Choose the first actual room.
        // const options = roomSelect.locator("option");
        //
        // const optionCount = await options.count();
        // expect(optionCount).toBeGreaterThan(1);
        //
        // const roomValue = await options.nth(1).getAttribute("value");
        // expect(roomValue).not.toBeNull();
        //
        // await roomSelect.selectOption(roomValue!);


        // Use a future time dynamically instead
        // of hard-coding 2030.
        const start = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);

        start.setSeconds(0, 0);

        const end = new Date(start.getTime() + 60 * 60 * 1000);

        const startValue = toDateTimeLocal(start);
        const endValue = toDateTimeLocal(end);

        await page.getByLabel("Start Time").fill(startValue);
        await page.getByLabel("End Time").fill(endValue);

        await page.getByRole("button", { name: "Book Room", }).click();

        const successMessage = page.getByText(/Booking \d+ created successfully\./);
        await expect(successMessage).toBeVisible();

        // Extract the ID of the booking
        // we just created.
        const successText = await successMessage.textContent();
        const match = successText?.match(/Booking (\d+)/);
        expect(match).not.toBeNull();

        const bookingId = Number(match![1]);

        // ---------------------------------
        // 3. View own bookings
        // ---------------------------------

        await page.goto("/bookings");
        const bookingHeading = page.getByRole("heading", { name: `Booking #${bookingId}`, });
        await expect(bookingHeading).toBeVisible();

        // ---------------------------------
        // 4. Cancel own booking
        // ---------------------------------

        const bookingCard = bookingHeading.locator("..");
        await bookingCard.getByRole("button", { name: "Cancel Booking", }).click();
        await expect(bookingHeading).toHaveCount(0);
    }
);

// test("user can create, view and cancel a booking", async ({ page, request, }) => {
//
//     page.on("console", message => {
//         console.log("BROWSER:", message.text());
//     });
//
//     const unique = Date.now();
//
//     // -----------------------------
//     // Arrange: create test user
//     // -----------------------------
//
//     const userResponse = await request.post(
//         "http://localhost:8080/api/users",
//         {
//             data: {
//                 name: "E2E User",
//                 email: `e2e-${unique}@example.com`,
//             },
//         }
//     );
//
//     expect(userResponse.ok()).toBeTruthy();
//
//     const testUser = await userResponse.json();
//
//     // -----------------------------
//     // Arrange: create test room
//     // -----------------------------
//
//     const roomResponse = await request.post(
//         "http://localhost:8080/api/rooms",
//         {
//             data: {
//                 name: `E2E Room ${unique}`,
//                 capacity: 4,
//             },
//         }
//     );
//
//     expect(roomResponse.ok()).toBeTruthy();
//
//     const testRoom = await roomResponse.json();
//
//     // -----------------------------
//     // Act: open booking page
//     // -----------------------------
//
//     await page.goto("/book");
//
//     // Fill User ID
//     await page.getByLabel("User ID").fill(String(testUser.id));
//
//     // Select the room we just created
//     await page.getByLabel("Room").selectOption(String(testRoom.id));
//
//     // Fill booking times
//     await page.getByLabel("Start Time").fill("2030-01-10T10:00");
//     await page.getByLabel("End Time").fill("2030-01-10T11:00");
//
//     // Submit
//     await page.getByRole("button", { name: "Book Room", }).click();
//
//     // -----------------------------
//     // Assert: booking was created
//     // -----------------------------
//
//     await expect(page.getByText(/Booking \d+ created successfully/)).toBeVisible();
//
//     // -----------------------------
//     // Act: open My Bookings
//     // -----------------------------
//
//     await page.getByRole("link", { name: "My Bookings", }).click();
//
//     // Debugging: log network requests and responses
//     page.on("request", request => {
//         console.log("REQUEST:", request.method(), request.url());
//     });
//     page.on("response", response => {
//         console.log("RESPONSE:", response.status(), response.url());
//     });
//
//     const userIdInput = page.getByLabel("User ID");
//     await userIdInput.click();
//     await userIdInput.pressSequentially(String(testUser.id));
//     await expect(userIdInput).toHaveValue(String(testUser.id));
//
//     // E2E-test adjustments:
//     // 1. fill() → pressSequentially() for User ID
//     // 2. "Start:" → "Start time:"
//     // 3. "Cancel booking" → "Cancel Booking"
//
//     // Debugging: log the value of the input field
//     console.log("USER ID INPUT:", await userIdInput.inputValue());
//
//     await page.getByRole("button", { name: "Load bookings", }).click();
//
//     // Debugging: wait for a moment to let the page update
//     await page.waitForTimeout(2000);
//     console.log("PAGE CONTENT:", await page.locator("body").innerText());
//
//     // -----------------------------
//     // Assert: booking appears
//     // -----------------------------
//
//     await expect(page.getByText(`Room ID: ${testRoom.id}`)).toBeVisible();
//     await expect(page.getByText("Start time: 2030-01-10T10:00:00")).toBeVisible();
//     await expect(page.getByText("End time: 2030-01-10T11:00:00")).toBeVisible();
//
//     // -----------------------------
//     // Act: cancel it
//     // -----------------------------
//
//     await page.getByRole("button", { name: "Cancel Booking", }).click();
//
//     // -----------------------------
//     // Assert: server state refreshed
//     // -----------------------------
//
//     await expect(page.getByText("No bookings found.")).toBeVisible();
// });