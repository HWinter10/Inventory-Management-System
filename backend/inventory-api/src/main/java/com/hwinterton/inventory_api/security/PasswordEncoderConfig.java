/*
Purpose: this file defines how the passwords are hased, enlisting a resusable PasswrodEncoder bean
and hashing the password with the BCrypt algorithm
*/
package com.hwinterton.inventory_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // using spring to mark this as a config class 
public class PasswordEncoderConfig {

    @Bean 
    public PasswordEncoder passwordEncoder() { // creates and returns password encoder for hashing
        return new BCryptPasswordEncoder(); 
    }
}
