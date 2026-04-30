package com.hwinterton.inventory_api.security;
/* 
Purpose: this class checks for JWT tokens and extracts username, then passes the info to 
        Spring. it passes along invalid requests as well because its main purpose is to try
        and authenticate, not to block requests. invalid requests just continue as unauthenticated

Dependencies needed:
- JwtUtil, UserDetailsServiceImpl

Pseudocode:
  doFilterInternal(request, response, filterChain):
    - ret Authorization header from request 
        - if header missing OR does not start with "Bearer " do nothing, continue request
    - extract token by removing "Bearer " prefix
    - extract username from token using JwtUtil
        - if username is null do nothing, continue request
    - validate token
        - if invalid do nothing, continue request
    - load user by username
    - set authentication using UserDetails
    - continue request to let spring decides if authorized
*/

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // Method - check request for JWT and set authentication if token is valid
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // get Authorization header
        String authHeader = request.getHeader("Authorization");

        // continue request if header is missing or not Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // remove Bearer prefix
        String token = authHeader.substring(7);

        // extract username from token
        String username = jwtUtil.extractUsername(token);

        // continue request if username missing
        if (username == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // continue request if token is invalid
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // load Spring Security user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // create authentication object
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        // attach request details
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // set authenticated user in Spring Security
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // continue request
        filterChain.doFilter(request, response);
    }
}