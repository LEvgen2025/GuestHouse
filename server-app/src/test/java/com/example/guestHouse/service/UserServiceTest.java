package com.example.guestHouse.service;

import com.example.guestHouse.repository.User;
import com.example.guestHouse.repository.UserRepository;
import com.example.guestHouse.repository.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User createTestUser(Long id, String username, String password, Set<Role> roles) {
        return new User(id, username, true, password, roles, LocalDateTime.now());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        Set<Role> roles = Set.of(Role.ROLE_USER);
        User user1 = createTestUser(1L, "user1", "pass1", roles);
        User user2 = createTestUser(2L, "user2", "pass2", roles);
        List<User> expectedUsers = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userService.findAll();

        // Assert
        assertEquals(expectedUsers, actualUsers);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        Set<Role> roles = Set.of(Role.ROLE_USER);
        User expectedUser = createTestUser(userId, "testUser", "password", roles);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> actualUser = userService.findUserById(userId);

        // Assert
        assertTrue(actualUser.isPresent());
        assertEquals(expectedUser, actualUser.get());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findUserById_WhenUserNotExists_ShouldReturnEmpty() {
        // Arrange
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> actualUser = userService.findUserById(userId);

        // Assert
        assertTrue(actualUser.isEmpty());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void createUser_WhenUsernameIsUnique_ShouldCreateUser() {
        // Arrange
        String rawPassword = "password";
        User newUser = new User(null, "newUser", false, rawPassword, new HashSet<>(), null);
        User savedUser = createTestUser(1L, "newUser", "encodedPassword", Set.of(Role.ROLE_USER));

        when(userRepository.findByUsername(newUser.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        boolean result = userService.createUser(newUser);

        // Assert
        assertTrue(result);
        assertTrue(newUser.isActive());
        assertEquals("encodedPassword", newUser.getPassword());
        assertTrue(newUser.getRoles().contains(Role.ROLE_USER));
        verify(userRepository, times(1)).findByUsername(newUser.getUsername());
        verify(userRepository, times(1)).save(newUser);
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    void createUser_WhenUsernameExists_ShouldNotCreateUser() {
        // Arrange
        Set<Role> roles = Set.of(Role.ROLE_USER);
        User existingUser = createTestUser(1L, "existingUser", "password", roles);
        User newUser = new User(null, "existingUser", false, "newPass", new HashSet<>(), null);

        when(userRepository.findByUsername(newUser.getUsername())).thenReturn(existingUser);

        // Act
        boolean result = userService.createUser(newUser);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findByUsername(newUser.getUsername());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void findUsernameById_ShouldReturnUsername() {
        // Arrange
        Long userId = 1L;
        String expectedUsername = "testUser";
        when(userRepository.findUsernameById(userId)).thenReturn(expectedUsername);

        // Act
        String actualUsername = userService.findUsernameById(userId);

        // Assert
        assertEquals(expectedUsername, actualUsername);
        verify(userRepository, times(1)).findUsernameById(userId);
    }

    @Test
    void changeUserRole_ShouldUpdateUserRole() {
        // Arrange
        Set<Role> initialRoles = new HashSet<>(Set.of(Role.ROLE_USER));
        User user = createTestUser(1L, "testUser", "password", initialRoles);
        Role newRole = Role.ROLE_ADMIN;

        // Act
        userService.changeUserRole(user, newRole);

        // Assert
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(newRole));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        Long userId = 1L;
        Set<Role> roles = Set.of(Role.ROLE_USER);
        User user = createTestUser(userId, "testUser", "password", roles);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldDoNothing() {
        // Arrange
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).deleteById(any());
    }
}