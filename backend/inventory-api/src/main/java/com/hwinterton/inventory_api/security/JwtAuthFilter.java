package com.hwinterton.inventory_api.security;

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
import lombok.extern.slf4j.Slf4j;

/**
 * Handles JWT authentication for incoming API requests.
 * 
 * <p>Checks requests for Bearer tokens, validates tokens using JwtUtil,
 * loads matching user details, and passes authenticated user information
 * into Spring Security.</p>
 * 
 * <p>This filter attempts authentication but does not make final authorization
 * decisions. Requests without valid tokens continue as unauthenticated, and
 * Spring Security later decides whether the endpoint should allow access.</p>
 * 
 * Authentication Flow:
 * <pre>
 * 1. Read Authorization header from request
 * 2. Check for Bearer token
 * 3. Extract JWT token
 * 4. Extract username from token
 * 5. Validate token
 * 6. Load user details
 * 7. Create Spring Security authentication object
 * 8. Store authentication in SecurityContext
 * 9. Continue request through filter chain
 * </pre>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Method - Checks each incoming request for a valid JWT Bearer token.
     * 
     * <p>If valid token is present, method loads matching user details and stores authentication
     * object in Spring Security's context for the current request. If token is missing or invalid,
     * request continues unauthenticated so Spring Security can make final access decision</p>
     * 
     * @param request the incoming HTTP request
     * @param response outgoing HTTP response
     * @param filterChain the remaining filter in request chain
     * @throws ServletException if the filter chain fails while processing request
     * @throws IOException if the filter chain fails while reading or writing request
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // get Authorization header
        String authHeader = request.getHeader("Authorization");

        // continue request if header is missing or not Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No Bearer token found for request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // remove Bearer prefix
        String token = authHeader.substring(7);

        // extract username from token
        String username = jwtUtil.extractUsername(token);

        // continue request if username missing
        if (username == null) {
            log.warn("JWT did not contain a username for request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // continue request if token is invalid
        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid JWT for username: {} on request: {}", username, request.getRequestURI());
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

        log.info("JWT authentication successful for user: {} on request: {}", username, request.getRequestURI());

        // Continue request through the filter chain.
        filterChain.doFilter(request, response);
    }
}