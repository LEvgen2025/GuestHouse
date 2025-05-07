package com.example.guestHouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "select * from users where username = :username", nativeQuery = true)
    User findByUsername(String username);

    @Query(value = "select username from users where id = :id", nativeQuery = true)
    String findUsernameById(Long id);
}
