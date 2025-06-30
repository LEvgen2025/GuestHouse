package com.example.guestHouse.service;

import com.example.guestHouse.repository.User;
import com.example.guestHouse.repository.UserRepository;
import com.example.guestHouse.repository.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public boolean createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) return false;
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(Role.ROLE_USER);
        userRepository.save(user);
        return true;
    }

    public String findUsernameById(Long id) {
        return userRepository.findUsernameById(id);
    }

    public void changeUserRole(User user, Role newRole) {
        // Очищаем старые роли и ставим новую
        user.getRoles().clear();
        user.getRoles().add(newRole);
        userRepository.save(user);  // Сохраняем изменения в БД
    }

    public void deleteUser(Long id) {
        userRepository.findById(id).ifPresent(user -> userRepository.deleteById(id));
    }
}
