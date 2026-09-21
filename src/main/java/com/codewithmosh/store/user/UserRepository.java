package com.codewithmosh.store.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// This code handles database access.

// Responsibility example: "Run the appropriate DB operation"
// PostgreSQL responsibility example: "Store the data"

@Repository // repository is the data-access layer.
public interface UserRepository extends JpaRepository<User, Long> { // class User and Long here is the type of the primary key in the DB which in this case is the user ID.

//    Spring gives us methods such as:
//        findAll()
//        findById(id)
//        save(user)
//        deleteById(id)
//        existsById(id)
//    without us implementing SQL ourselves.

//       UserRepository
//      ↓
//       database operations
//      ↓
//       users table

    Optional<User> findByKeycloakSubject(String keycloakSubject); // This is a custom query method that Spring Data JPA will implement for us. It will find a User entity by its keycloakSubject field. The method name follows the Spring Data JPA naming convention, so Spring will generate the appropriate SQL query automatically.
    Optional<User> findByEmail(String email); // This is another custom query method that Spring Data JPA will implement for us. It will find a User entity by its email field. The method name follows the Spring Data JPA naming convention, so Spring will generate the appropriate SQL query automatically.
}
