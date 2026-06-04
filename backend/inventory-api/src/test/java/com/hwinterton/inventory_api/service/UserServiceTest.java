package com.hwinterton.inventory_api.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hwinterton.inventory_api.dto.user.UserPasswordResponse;
import com.hwinterton.inventory_api.dto.user.UserRequest;
import com.hwinterton.inventory_api.dto.user.UserResponse;
import com.hwinterton.inventory_api.model.Role;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void getAllUsers_returnsListOfUserResponses() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("owner");
        user1.setRole(Role.OWNER);
        user1.setMustChangePassword(false);
        user1.setActive(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("staff");
        user2.setRole(Role.STAFF);
        user2.setMustChangePassword(true);
        user2.setActive(true);

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<UserResponse> response = userService.getAllUsers();

        assertEquals(2, response.size());
        assertEquals("owner", response.get(0).username());
        assertEquals(Role.OWNER, response.get(0).role());
        assertEquals("staff", response.get(1).username());
        assertEquals(Role.STAFF, response.get(1).role());

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_whenUserExists_returnsUserResponse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("manager");
        user.setRole(Role.MANAGER);
        user.setMustChangePassword(false);
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertEquals(1L, response.id());
        assertEquals("manager", response.username());
        assertEquals(Role.MANAGER, response.role());
        assertFalse(response.mustChangePassword());
        assertTrue(response.active());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_whenUserMissing_throwsRuntimeException() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                userService.getUserById(99L)
        );

        verify(userRepository).findById(99L);
    }

    @Test
    void createUser_whenUsernameIsUnique_createsUserWithTemporaryPassword() {
        UserRequest request = new UserRequest("newstaff", Role.STAFF);

        when(userRepository.existsByUsername("newstaff"))
                .thenReturn(false);

        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("hashed-temp-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(10L);
                    return user;
                });

        UserPasswordResponse response = userService.createUser(request);

        assertEquals(10L, response.userId());
        assertEquals("newstaff", response.username());
        assertNotNull(response.temporaryPassword());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("newstaff", savedUser.getUsername());
        assertEquals("hashed-temp-password", savedUser.getPasswordHash());
        assertEquals(Role.STAFF, savedUser.getRole());
        assertTrue(savedUser.isMustChangePassword());
        assertTrue(savedUser.isActive());

        verify(userRepository).existsByUsername("newstaff");
        verify(passwordEncoder).encode(response.temporaryPassword());
    }

    @Test
    void createUser_whenUsernameExists_throwsIllegalArgumentException() {
        UserRequest request = new UserRequest("owner", Role.OWNER);

        when(userRepository.existsByUsername("owner"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(request)
        );

        verify(userRepository).existsByUsername("owner");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }

    @Test
    void updateUser_whenUsernameIsAvailable_updatesUser() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("staff");
        existingUser.setRole(Role.STAFF);
        existingUser.setMustChangePassword(false);
        existingUser.setActive(true);

        UserRequest request = new UserRequest("manager", Role.MANAGER);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.existsByUsername("manager"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals(1L, response.id());
        assertEquals("manager", response.username());
        assertEquals(Role.MANAGER, response.role());

        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("manager");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_whenUsernameBelongsToSameUser_doesNotThrowDuplicateError() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("staff");
        existingUser.setRole(Role.STAFF);
        existingUser.setMustChangePassword(false);
        existingUser.setActive(true);

        UserRequest request = new UserRequest("staff", Role.MANAGER);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("staff", response.username());
        assertEquals(Role.MANAGER, response.role());

        verify(userRepository, never()).existsByUsername("staff");
        verify(userRepository).save(existingUser);
    }

    @Test
    void deactivateUser_setsActiveFalse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("staff");
        user.setRole(Role.STAFF);
        user.setMustChangePassword(false);
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.deactivateUser(1L);

        assertFalse(response.active());
        assertFalse(user.isActive());

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void reactivateUser_setsActiveTrue() {
        User user = new User();
        user.setId(1L);
        user.setUsername("staff");
        user.setRole(Role.STAFF);
        user.setMustChangePassword(false);
        user.setActive(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.reactivateUser(1L);

        assertTrue(response.active());
        assertTrue(user.isActive());

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_generatesTemporaryPasswordAndRequiresPasswordChange() {
        User user = new User();
        user.setId(1L);
        user.setUsername("staff");
        user.setRole(Role.STAFF);
        user.setPasswordHash("old-hash");
        user.setMustChangePassword(false);
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("new-hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserPasswordResponse response = userService.resetPassword(1L);

        assertEquals(1L, response.userId());
        assertEquals("staff", response.username());
        assertNotNull(response.temporaryPassword());

        assertEquals("new-hashed-password", user.getPasswordHash());
        assertTrue(user.isMustChangePassword());

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode(response.temporaryPassword());
        verify(userRepository).save(user);
    }
}