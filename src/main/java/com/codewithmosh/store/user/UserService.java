package com.codewithmosh.store.user;

import org.springframework.stereotype.Service;
import java.util.List;

// Service handles business logic
// Responsibility example: "Find user 5 and update its information"

@Service
public class UserService {

    private final UserRepository userRepository;

    // Here we are using constructor injection to inject the UserRepository dependency into the UserService class. This is a common practice in Spring applications to promote loose coupling and easier testing.
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }
    // Update to this in Jakarta EE REST version:
//    public UserResponse createUser(UserRequest request) {
//
//        User user = new User();
//        user.setName(request.name());
//        user.setEmail(request.email());
//
//        User savedUser = userRepository.save(user);
//
//        return new UserResponse(
//                savedUser.getId(),
//                savedUser.getName(),
//                savedUser.getEmail()
//        );
//    }

    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

}


// CDI (Jakarta EE) Version:

//@ApplicationScoped