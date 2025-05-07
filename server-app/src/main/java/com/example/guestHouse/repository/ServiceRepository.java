package com.example.guestHouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository <Service, Long> {

    @Query(value = "select * from services where name = :name", nativeQuery = true)
    Optional<Service> findByName(String name);

    @Query(value = "select * from services order by name", nativeQuery = true)
    List<Service> findAllSortedByAlphabet();
}
