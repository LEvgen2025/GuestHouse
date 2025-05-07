package com.example.guestHouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {

//    @Query(value = "select * " +
//            "from rentals " +
//            "where house = :house AND client = :client AND (start_date = :startDate OR end_date = :endDate)",nativeQuery = true)
//    Optional<Rental> findByRentalDate(House house, Client client, LocalDate startDate, LocalDate endDate);
    @Query("SELECT r FROM Rental r WHERE r.house = :house AND r.client = :client AND r.startDate = :startDate AND r.endDate = :endDate AND r.id <> :id")
    Optional<Rental> findByRentalDate(House house, Client client, LocalDate startDate, LocalDate endDate, Long id);

    @Query(value = "select * from rentals order by client", nativeQuery = true)
    List<Rental> findAllSortedByClients();
}
