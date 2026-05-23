package com.hwinterton.inventory_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.User;

/**
 * Repository for User database access.
 *
 * <p>Extends JpaRepository to inherit common CRUD methods and defines
 * user-specific lookup methods needed for authentication and user management.</p>
 */
public interface UserRepository extends JpaRepository<User, Long>{
    /*
     * JpaRepository built-in methods such as:
     * save(), findById(), findAll(), deleteById(), existsById(), and count().
     */

    // used by authentication to load user from database
    Optional<User> findByUsername(String username);

    // used before creating users to prevent duplicate usernames
    boolean existsByUsername(String username);
}
