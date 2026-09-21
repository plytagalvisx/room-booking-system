package com.plytagalvisx.roombooking.auth;

import com.plytagalvisx.roombooking.user.AuthenticatedUserService;
import com.plytagalvisx.roombooking.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashMap;
import java.util.Map;

// Controller contains the endpoint mappings and delegates the business logic to the service layer.
// It handles HTTP requests and responses, and it is responsible for returning data to the client
// in a format that the client can understand (e.g., JSON).

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Inject the service layer (if needed) to handle business logic related to authentication.
    private final AuthenticatedUserService authenticatedUserService;

    public AuthController(AuthenticatedUserService authenticatedUserService) {
        this.authenticatedUserService = authenticatedUserService;
    }

//    @GetMapping("/me")
//    public Map<String, String> me(@AuthenticationPrincipal Jwt jwt) {
//        Map<String, String> user = new HashMap<>();
//
//        user.put("subject", jwt.getSubject());
//        user.put("username", jwt.getClaimAsString("preferred_username"));
//        user.put("email", jwt.getClaimAsString("email"));
//
//        return user;
//    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        User user = authenticatedUserService.getOrCreateUser(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("subject", user.getKeycloakSubject());
        result.put("username", user.getName());
        result.put("email", user.getEmail());
        return result;
    }

    @GetMapping("/roles")
    public Map<String, Object> roles(Authentication authentication) {
        return Map.of(
                "name",
                authentication.getName(),
                "authorities",
                authentication
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}

// Later, when authenticated, this might return:
//{
//    "subject": "02b813aa-...",
//    "username": "milena",
//    "email": "milena@example.com"
//}

// This is our first glimpse of how Spring gets identity from the JWT rather than from a user ID typed into React.

// Because we supplied an explicit JWK-set URI as well as issuer information,
// Spring doesn't need to perform issuer discovery against Keycloak during application startup.