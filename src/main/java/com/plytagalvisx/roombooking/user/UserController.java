package com.plytagalvisx.roombooking.user;

import org.springframework.web.bind.annotation.*;
import java.util.List;

// Controller handles any HTTP concerns (like saving an HTTP response (e.g., JSON text) to a database)
// Our controller is based on Spring MVC (Model-View-Controller) architecture.
// The controller is responsible for handling incoming HTTP requests, processing them, and returning appropriate HTTP responses. It acts as an intermediary between the client (e.g., a web browser or mobile app) and the service layer (business logic) of the application.

// Responsibility example: "Someone called PUT /api/users/5"

// GET /api/users/5
//        ↓
// UserController
//        ↓
// "Someone wants User 5"
//        ↓
// UserService

// The controller shouldn't care very much about how User 5 gets retrieved.
// Our service does that.

@RestController
@RequestMapping("/api/users") // (baseline API URL) when we go to this mapping in the URL, we will call different controllers, different functions.
public class UserController {
    // To make this user controller work we need to use dependency injection (composition); so we want to inject the user repository here.
//    @Autowired // @Autowired tells the Spring framework to automatically inject an instance of UserRepository into our UserController
//    private UserRepository userRepository;

    // A constructor (dependency) injection is generally preferred over field injection with @Autowired. Like so:
//    private final UserRepository userRepository;
//
//    public UserController(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    private final UserService userService;

    public UserController(UserService userService) { // This is constructor injection, which is generally preferred over field injection with @Autowired.
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
//        return userRepository.findAll();
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) { // @PathVariable approach is preferable here instead of @RequestParam because we want this to be in the URL path. @PathVariable (Path Parameter). @RequestParam (Query Parameter). Use @PathVariable when you want to open one specific file (e.g., User #5, Product #102). Use @RequestParam when you want to search, sort, or filter through a big pile of files (e.g., status=active, page=2).
//        return userRepository.findById(id).get();
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) { // @RequestBody here means that we will have the body in JSON format for the user.
//        return userRepository.save(user);
        return userService.createUser(user);
    }
    // Update to this in Jakarta EE REST version:
//    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
//        return userService.createUser(request);
//    }

    @PutMapping("/{id}") // The put REST (HTTP) request is to modify an existing user
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        // TODO: Don't forget to handle errors here as well
//        User existingUser = userRepository.findById(id).get();
//        existingUser.setName(user.getName());
//        existingUser.setEmail(user.getEmail());
//        return userRepository.save(existingUser);

        // TODO: Don't forget to handle errors here as well
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        try {
//            userRepository.findById(id).get(); // Check if the user exists before deleting. If not, it will throw an exception and we can handle it in the catch block.
//            userRepository.deleteById(id);

            userService.getUserById(id); // Check if the user exists before deleting. If not, it will throw an exception and we can handle it in the catch block.
            userService.deleteUser(id);
            return "User deleted successfully";
        } catch (Exception e) { // catches everything. Even when PostgreSQL crashes
            // TODO: Handle specific failures instead of catching Exception (because we can catch other exceptions here
            //  as well, not just the user not found exception)
            return "User not found";
        }
    }

}


// Jakarta EE REST version:
//@Path("users")



// When using @Autowired:
// Instead of us manually writing code to create the object (like userRepository = new UserRepository()),
// Spring handles the object creation, management, and setup behind the scenes.
// This software design pattern is known as Dependency Injection or Inversion of Control (IoC).

// Why do we use it?:
// 1. Separation of Concerns:
// Our controller doesn't need to know how to connect to the database or implement the repository.
// It just asks Spring for a working tool and uses it.
//
// 2. Looser Coupling:
// It decouples our classes. If we ever change how UserRepository is implemented,
// we don't need to change any code inside UserController.

// 3. Easier Testing:
// When writing unit tests, we can easily swap out the real database repository for a fake, mock repository.
//
// How it works in this code:
// 1. When our application starts up, Spring scans our project and finds UserRepository.
// 2. It automatically creates and manages an instance of it (called a Spring Bean).
// 3. When Spring loads our UserController, it sees @Autowired right above private UserRepository userRepository;.
// 4. Spring looks into its container, finds the UserRepository bean it just made, and plugs it
//    straight into that variable so we can use methods like userRepository.findAll().

// OBS! While field injection (putting @Autowired directly on the variable) works perfectly fine,
// modern Spring Boot best practices actually prefer Constructor Injection.

// Example with @Autowired (Field Injection):
// Spring finds the hidden field and forcefully injects the dependency directly into it using Java reflection.
//@RestController
//@RequestMapping("/api/users")
//public class UserController {
//
//    @Autowired // <-- Tells Spring to inject this field behind the scenes
//    private UserRepository userRepository;
//
//    @GetMapping
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//}


// Example without @Autowired (Constructor Injection): (OBS! Preferable choice?)
// This is the modern, recommended approach. Since Spring Framework 4.3, if a class has only one constructor,
// Spring will automatically inject the dependencies without needing the @Autowired annotation at all.
//@RestController
//@RequestMapping("/api/users")
//public class UserController {
//
//    // 1. Mark the field as 'final' so it cannot be changed after creation
//    private final UserRepository userRepository;
//
//    // 2. Provide a constructor. Spring calls this and passes the repository automatically.
//    public UserController(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @GetMapping
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//}