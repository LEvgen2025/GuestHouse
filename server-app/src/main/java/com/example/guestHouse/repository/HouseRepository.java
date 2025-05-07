package com.example.guestHouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HouseRepository extends JpaRepository<House, Long> {

    @Query(value = "select * from houses where name = :name", nativeQuery = true)
    Optional<House> findByName(String name);

    @Query(value = "select * from houses order by name", nativeQuery = true)
    List<House> findAllSortedByAlphabet();
}
