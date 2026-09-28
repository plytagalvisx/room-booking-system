function requireEnv(name: string, value: string | undefined): string {
    if (!value) {
        throw new Error(`Missing required environment variable: ${name}`);
    }

    return value;
}

export const env = {
    apiBaseUrl: requireEnv("VITE_API_BASE_URL", import.meta.env.VITE_API_BASE_URL),
    keycloakUrl: requireEnv("VITE_KEYCLOAK_URL", import.meta.env.VITE_KEYCLOAK_URL),
    keycloakRealm: requireEnv("VITE_KEYCLOAK_REALM", import.meta.env.VITE_KEYCLOAK_REALM),
    keycloakClientId: requireEnv("VITE_KEYCLOAK_CLIENT_ID", import.meta.env.VITE_KEYCLOAK_CLIENT_ID),
};