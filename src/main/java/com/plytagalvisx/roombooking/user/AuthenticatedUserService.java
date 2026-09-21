package com.plytagalvisx.roombooking.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateUser(String subject, String username, String email) {
        // It will find a User entity by its keycloakSubject field.
        return userRepository.findByKeycloakSubject(subject).orElseGet(() -> createOrLinkUser(subject, username, email));
    }

    private User createOrLinkUser(String subject, String username, String email) {
        // If the email is present, check if a user with that email already exists
        if (email != null) {
            var existingUser = userRepository.findByEmail(email);
            // If a user with the same email is found, link it to the Keycloak subject
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                user.setKeycloakSubject(subject);
                return userRepository.save(user);
            }
        }

        // If no existing user is found, create a new user
        User user = new User();
        user.setName(username);
        user.setEmail(email);
        user.setKeycloakSubject(subject);
        return userRepository.save(user);
    }
}


//    React
//      │
//      │ JWT
//      ▼
//    Spring Security
//      │
//      │ "This JWT belongs to Milena"
//      ▼
//    users.keycloak_subject
//      │
//      ▼
//    User entity
//      │
//      ▼
//    Booking