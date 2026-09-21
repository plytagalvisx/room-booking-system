import keycloak from "../../auth/keycloak";

type RealmAccess = {
    roles?: string[]; // question mark means optional, because the roles property may not be present in the token
};

// Role helper function to check if the user has a specific realm role such as "ADMIN" or "USER".
// This function checks the roles in the Keycloak token parsed object.
// It returns true if the user has the specified role, and false otherwise.
export function hasRealmRole(role: string): boolean {
    const realmAccess = keycloak.tokenParsed?.realm_access as RealmAccess | undefined;

    return (
        realmAccess?.roles?.includes(role) ?? false
    );
}

// Now React can ask:
//
//     hasRealmRole("ADMIN")
//
// instead of manually digging through:
//
//     tokenParsed
//     → realm_access
//     → roles