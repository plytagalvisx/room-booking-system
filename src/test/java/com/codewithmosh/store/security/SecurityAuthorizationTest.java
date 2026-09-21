package com.codewithmosh.store.security;

import com.codewithmosh.store.common.security.KeycloakRealmRoleConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

// SecurityAuthorizationTest
// → Does Spring enforce ROLE_ADMIN correctly?

// This is an integration test:

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void creatingRoomWithoutAuthenticationReturns401() throws Exception {

        String json = """
            {
              "name": "Security Test Room",
              "capacity": 5
            }
            """;

        mockMvc.perform(
                        post("/api/rooms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRoleCannotCreateRoom() throws Exception {

        String json = """
            {
              "name": "Security Test Room",
              "capacity": 5
            }
            """;

        // OBS! Spring's jwt() test helper lets us supply authorities directly,
        // so we don't need a signed Keycloak token just to test hasRole("ADMIN").
        mockMvc.perform(
                        post("/api/rooms")
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoleCanCreateRoom() throws Exception {

        String json = """
            {
              "name": "Admin Security Room",
              "capacity": 5
            }
            """;

        mockMvc.perform(
                        post("/api/rooms")
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().is2xxSuccessful());
    }
}

// One subtle point: these MockMvc JWT tests deliberately do not cryptographically validate a real Keycloak token.
// Spring Security's test helper injects an authenticated JWT so that tests can focus on authorization rules.