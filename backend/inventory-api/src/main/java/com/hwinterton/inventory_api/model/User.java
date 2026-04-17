// User is our base object here, with this being anyone from the owner to staff members. We
// create the user table using JPA and have it autogenerate ids, then we allow a user to set up and 
// retreive other users using the auto generated getter and setters libraries in Lombok.
package com.hwinterton.inventory_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: auto generates getters and setters for all field
@NoArgsConstructor // Lombok: auto generates no-args contructor required by JPA
@Entity // JPA: maps class to datatable (uses table, comumn, id, generatedVal, enumerated)
@Table(name = "users")
public class User {
    // Database column setup
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true) // non nullable means it is a required field
    private String username;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING) // our enum object
    @Column(nullable = false)
    private Role role;
    
    @Column(nullable = false)
    private boolean mustChangePassword = true;
    
    @Column(nullable = false)
    private  boolean active = true; // makes all new users auto active
}
