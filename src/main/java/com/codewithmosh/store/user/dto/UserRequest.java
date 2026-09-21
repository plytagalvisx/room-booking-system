package com.codewithmosh.store.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank String name,
        @NotBlank @Email String email
) {
}


// Option 1: The All-in-One Dependency (Recommended)If you are deploying your application to a full Jakarta EE compatible
// application server (like WildFly, Payara, or GlassFish), you can bring in the entire Jakarta EE API with a single dependency.
// Because the server provides the actual implementation, you should set the <scope> to provided.

// <dependency>
//    <groupId>jakarta.platform</groupId>
//    <artifactId>jakarta.enterprise.jakartaee-api</artifactId>
//    <version>10.0.0</version> <!-- Use 11.0.0 if you are using Jakarta EE 11 -->
//    <scope>provided</scope>
// </dependency>

// OBS!
// Do not add this dependency inside your Java SpringBoot pom.xml. If you add this full Jakarta platform dependency
// to a standard Spring Boot application, it will cause dependency pollution and runtime crashes.
// Spring Boot already packages its own highly tuned implementations of specific Jakarta specs
// (like Tomcat for Servlets, and Hibernate for JPA).
// Adding the blanket jakartaee-api dependency with <scope>provided</scope> tells Maven,
// "Hey, my server already has all of Jakarta EE built-in!". But standard Spring Boot Tomcat does not,
// leading to ClassNotFoundException runtime errors.