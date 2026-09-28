import Keycloak from "keycloak-js";
import { env } from "../config/env";

const keycloak = new Keycloak({
    url: env.keycloakUrl, // import.meta.env.VITE_KEYCLOAK_URL
    realm: env.keycloakRealm, // import.meta.env.VITE_KEYCLOAK_REALM
    clientId: env.keycloakClientId, // import.meta.env.VITE_KEYCLOAK_CLIENT_ID
});

export default keycloak;

// This code executes in the browser.
// React JavaScript
//       ↓
// browser
//       ↓
// localhost:8081
//       ↓
// Docker forwards 8081 → 8080
//       ↓
// Keycloak

// The browser is outside Docker, so keycloak:8080 is not the appropriate hostname here.


// Make React actually log in and obtain a real JWT.

// Keycloak's official JavaScript adapter is keycloak-js. It uses OpenID Connect (OIDC) identity authentication protocol
// and, by default, the Authorization Code flow; PKCE can be enabled with S256. Keycloak also recommends that browser
// applications be public clients because a browser cannot securely keep a client secret (credentials).

// OpenID Connect (OIDC) is a simple identity authentication protocol built on top of the OAuth 2.0 framework.
// It lets client applications verify user identity and obtain basic profile info from an authorization server.

// We are not installing some React-specific authentication framework yet. keycloak-js is enough to understand what's actually happening.

// React :5173
//     │
//     │ Login
//     ▼
// Keycloak :8081
//     │
//     │ username/password
//     │
//     │ issues JWT
//     ▼
// React
//     │
//     │ Authorization: Bearer <JWT>
//     ▼
// Spring :8080
//     │
//     │ validates JWT
//     ▼
// GET /api/auth/me
//     │
//     ▼
// 200 OK + authenticated user