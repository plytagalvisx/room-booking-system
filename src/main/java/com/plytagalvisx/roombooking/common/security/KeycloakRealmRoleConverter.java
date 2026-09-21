package com.plytagalvisx.roombooking.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// Convert Keycloak roles to Spring authorities.

@Component
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null) {
            return Collections.emptyList();
        }

        Object rolesObject = realmAccess.get("roles");

        if (!(rolesObject instanceof Collection<?> roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(Object::toString)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}

// Next is mapping the Keycloak realm roles inside the JWT into Spring Security authorities.
// But Spring Security does not automatically interpret Keycloak's realm_access.roles as ROLE_USER / ROLE_ADMIN.
// We need to teach it how.

// Example of JWT containing realm_access claim:
//
// "realm_access": {
//    "roles": [
//        "USER",
//        "ADMIN",
//        "offline_access",
//        "uma_authorization"
//    ]
// }