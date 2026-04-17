// User repo is used to move traffic around for user objects. it does not contain logic
// and uses JPA build in methods to reduce boilerplate
package com.hwinterton.inventory_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
    // JpaRepository built-ins: 
        // save(user),
        // findById(id),
        // findAll(),
        // deleteById(id),
        // existsById(id)
        
    // optional means results might be empty which forces the caller to handle
    // such cases where no user exists with that username
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
