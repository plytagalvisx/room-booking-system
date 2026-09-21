package com.codewithmosh.store.security;

import com.codewithmosh.store.common.security.KeycloakRealmRoleConverter;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// KeycloakRealmRoleConverterTest
// → Does JWT role data become ROLE_ADMIN/ROLE_USER?

// This is a unit test:

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void convertsKeycloakRealmRolesToSpringAuthorities() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("test-user")
                .claim("realm_access", Map.of("roles", List.of("USER", "ADMIN")))
                .build();

        var authorities = converter.convert(jwt);

        assertThat(authorities)
                .extracting("authority")
                .contains(
                        "ROLE_USER",
                        "ROLE_ADMIN"
                );
    }

    @Test
    void returnsNoAuthoritiesWhenRealmAccessIsMissing() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("test-user")
                .build();

        var authorities = converter.convert(jwt);

        assertThat(authorities).isEmpty();
    }
}