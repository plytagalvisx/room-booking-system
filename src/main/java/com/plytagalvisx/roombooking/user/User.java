package com.plytagalvisx.roombooking.user;

import jakarta.persistence.*; // Spring Data JPA

// User is a JPA entity mapped to the users database table.
// Once we have a JPA entity, we want to create a Flyway database migration for creating this table.
// OBS! Instead of creating a migration script manually, we can leverage IntelliJ IDEA feature to
// generate Flyway database migration script from the JPA entities.

// Model class
@Entity // We want this class to be synchronized with the database thus the annotation @Entity. @Entity tells JPA/Hibernate that this class represents an entity. Whether Hibernate actually creates/updates tables depends on configuration such as: spring.jpa.hibernate.ddl-auto=update
@Table(name = "users")
public class User {

    @Id // an incremental user ID property
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // Long is Java's object/wrapper representation of a 64-bit integer.
                        // Using Long instead of long also allows null.
                        // Long is commonly used for entity IDs because a newly created Java object can have: id == null until PostgreSQL assigns an ID.

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true) // these fields are valid, but the annotations aren't necessary because the Java names and database column names are already identical.
    private String email;

    @Column(name = "keycloak_subject", unique = true)
    private String keycloakSubject;

    // getters and setters:
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKeycloakSubject() { return keycloakSubject; }

    public void setKeycloakSubject(String keycloakSubject) { this.keycloakSubject = keycloakSubject; }
}
