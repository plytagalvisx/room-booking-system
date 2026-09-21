package com.plytagalvisx.roombooking.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

// This is the standard Spring Security structure for a JWT resource server: SecurityFilterChain plus oauth2ResourceServer(...jwt...).

// Here we define security rules for our REST API endpoints.

// We want to allow unauthenticated users to view rooms (GET requests),
// but require authentication for creating and viewing their own bookings (POST requests).
// We also want to restrict access to certain endpoints based on user roles (e.g., only admins can create rooms).

@Configuration
public class SecurityConfig {

    private final KeycloakRealmRoleConverter keycloakRealmRoleConverter;

    public SecurityConfig(KeycloakRealmRoleConverter keycloakRealmRoleConverter) {
        this.keycloakRealmRoleConverter = keycloakRealmRoleConverter;
    }

    // Connect the converter to Spring Security.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(keycloakRealmRoleConverter);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable()) // For this bearer-token REST setup we're disabling CSRF protection because we're not authenticating API requests using a browser session cookie.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // means Spring won't create a traditional server-side login session.
                                                                                                                // Each request proves its identity independently:
                                                                                                                //        Request 1
                                                                                                                //        Authorization: Bearer JWT
                                                                                                                //         ↓
                                                                                                                //        validate
                                                                                                                //
                                                                                                                //        Request 2
                                                                                                                //        Authorization: Bearer JWT
                                                                                                                //         ↓
                                                                                                                //        validate again
                                                                                                                // That's a natural architecture for a React SPA + REST API.
                .authorizeHttpRequests(auth -> auth
                        // Temporary while learning authentication:
                        .requestMatchers("/api/auth/**")
                        .authenticated()

                        // Now Spring Security must require authentication for creating bookings.
                        // We change the authorization section to (order matters).
                        // OBS! This is done to protect the POST /api/bookings endpoint, which is used to create new bookings. We want to ensure that only authenticated users can create bookings, while still allowing unauthenticated users to view bookings (GET requests).
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/bookings"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bookings/me"
                        )
                        .authenticated()

                        .requestMatchers( // the DELETE /api/bookings/** endpoint is protected by authenticated() method. We want to ensure that only authenticated users can delete bookings.
                                HttpMethod.DELETE,
                                "/api/bookings/**"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/rooms"
                        )
                        .hasRole("ADMIN")

                        // Allow all other requests to be permitted:
                        .requestMatchers("/api/**")
                        .permitAll()

                        .anyRequest()
                        .permitAll()
                )
//                .oauth2ResourceServer(oauth2 ->
//                        oauth2.jwt(Customizer.withDefaults()) // Authentication for protected HTTP requests comes from OAuth2 Bearer JWTs.
//                );                                            // So eventually Spring expects:
                                                              // Authorization: Bearer eyJhbGciOi...
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }
}

// So Conceptually (the updated oauth2ResourceServer does this):
//    JWT arrives
//       ↓
//    Spring verifies signature / issuer / expiry
//       ↓
//    KeycloakRealmRoleConverter
//       ↓
//    realm_access.roles
//       ↓
//    ROLE_USER
//            ROLE_ADMIN
//       ↓
//    Spring Authentication object



// Spring checks these rules from top to bottom:
//    POST /api/bookings
//            ↓
//    matches authenticated() first
//            ↓
//    JWT required

// It therefore never reaches the broader:
//      .requestMatchers("/api/**").permitAll()
// for that request.