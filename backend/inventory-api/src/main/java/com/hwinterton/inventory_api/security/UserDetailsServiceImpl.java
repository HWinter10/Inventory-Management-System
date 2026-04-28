package com.hwinterton.inventory_api.security;
/*
Purpose: goes to UserRepo, fetches user entity data and outputs it into a UserDetails object
         that Spring Security can understand without touching the database.

Dependencies needed:
- UserRepository, User, Role

Methods:
  loadUserByUsername(username):
    - look up user by username
        - if not found throw UsernameNotFoundException
    - build UserDetails object containing:
        - username
        - password hash
        - role as a GrantedAuthority
        -isEnabled = user.isActive()
    - return UserDetails
 */
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.UserRepository;

@Service // notifies spring so DaoAuthenticationProvider can find and use this class
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    // Method to look up user by username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // look up user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // convert role to GrantedAuthority
        GrantedAuthority authority = 
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // build and return UserDetails object
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(authority))
                .disabled(!user.isActive())
                .build();
    }
}
