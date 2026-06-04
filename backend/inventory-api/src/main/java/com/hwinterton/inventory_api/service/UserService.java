package com.hwinterton.inventory_api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hwinterton.inventory_api.dto.user.UserPasswordResponse;
import com.hwinterton.inventory_api.dto.user.UserRequest;
import com.hwinterton.inventory_api.dto.user.UserResponse;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.UserRepository;

/**
 * Service for user management business logic.
 *
 * <p>Handles user lookup, creation, role updates, account activation status,
 * and temporary password reset workflows.</p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Method: retrieves all users.
     *
     * @return a list of user response DTOs
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Method: retrieves one user by ID.
     *
     * @param id the user ID
     * @return the matching user as a response DTO
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);

        return toResponse(user);
    }

    /**
     * Method: creates a new user with a temporary password.
     *
     * <p>New users are created as active and must change their password
     * before using the system normally.</p>
     *
     * @param request the user creation data sent from the frontend
     * @return the created user's temporary password response
     */
    @Transactional
    public UserPasswordResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setRole(request.role());
        user.setMustChangePassword(true);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return new UserPasswordResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                temporaryPassword
        );
    }

    /**
     * Method: updates an existing user's username and role.
     *
     * <p>Does not update password or account activation status.</p>
     *
     * @param id the user ID
     * @param request the updated user data sent from the frontend
     * @return the updated user as a response DTO
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findUserById(id);

        if (!user.getUsername().equals(request.username())
                && userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setUsername(request.username());
        user.setRole(request.role());

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    /**
     * Method: deactivates a user account.
     *
     * <p>This is a soft delete. The user remains in the database but can no
     * longer log in while inactive.</p>
     *
     * @param id the user ID
     * @return the deactivated user as a response DTO
     */
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = findUserById(id);

        user.setActive(false);

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    /**
     * Method: reactivates a user account.
     *
     * @param id the user ID
     * @return the reactivated user as a response DTO
     */
    @Transactional
    public UserResponse reactivateUser(Long id) {
        User user = findUserById(id);

        user.setActive(true);

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    /**
     * Method: resets a user's password to a temporary password.
     *
     * <p>The user must change the temporary password before using the system
     * normally again.</p>
     *
     * @param id the user ID
     * @return the temporary password response
     */
    @Transactional
    public UserPasswordResponse resetPassword(Long id) {
        User user = findUserById(id);

        String temporaryPassword = generateTemporaryPassword();

        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);

        User updatedUser = userRepository.save(user);

        return new UserPasswordResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                temporaryPassword
        );
    }

    // Method: finds a user or throws a consistent exception message.
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Method: converts User entity data into the response shape used by the frontend.
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isMustChangePassword(),
                user.isActive()
        );
    }

    // Method: generates a short temporary password for new users and password resets.
    private String generateTemporaryPassword() {
        return "Temp-" + UUID.randomUUID().toString().substring(0, 8);
    }
}