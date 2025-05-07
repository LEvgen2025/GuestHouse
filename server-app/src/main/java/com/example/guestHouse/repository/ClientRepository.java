package com.example.guestHouse.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query(value = "select * from clients where phone_number = :phoneNumber", nativeQuery = true)
    Optional<Client> findByPhoneNumber(String phoneNumber);

    @Query(value = "select * from clients order by name", nativeQuery = true)
    List<Client> findAllSortedByAlphabet();
}
