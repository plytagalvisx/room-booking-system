import "@testing-library/jest-dom/vitest";


// That gives us assertions such as:
// expect(element).toBeInTheDocument();
// expect(button).toBeDisabled();
// expect(input).toHaveValue(...);

import { cleanup } from "@testing-library/react"; // Each test should start with a clean DOM. Else our tests will be leaving previously rendered forms in the DOM.
import { afterEach } from "vitest";

afterEach(() => {
    cleanup();
});