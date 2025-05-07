package com.example.guestHouse.service;

import com.example.guestHouse.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final HouseRepository houseRepository;
    private final ClientRepository clientRepository;

    public RentalService(RentalRepository rentalRepository, HouseRepository houseRepository, ClientRepository clientRepository) {
        this.rentalRepository = rentalRepository;
        this.houseRepository = houseRepository;
        this.clientRepository = clientRepository;
    }

    public List<Rental> findAll() {
        return rentalRepository.findAllSortedByClients();
    }

    public Rental create(Rental rental) {
        Optional<House> optionalHouse = houseRepository.findById(rental.getHouse().getId());
        if (optionalHouse.isEmpty()) {
            throw new IllegalStateException("Дома с id "+rental.getHouse().getId()+" не существует");
        }
        Optional<Client> optionalClient = clientRepository.findById(rental.getClient().getId());
        if (optionalClient.isEmpty()) {
            throw new IllegalStateException("Клиента с id "+rental.getClient().getId()+" не существует");
        }

        return rentalRepository.save(rental);
    }

    public void delete(Long id){
        Optional<Rental> optionalRental = rentalRepository.findById(id);
        if (optionalRental.isEmpty()) {
            throw new IllegalArgumentException("Аренды с id "+id+" не существует");
        }

        rentalRepository.deleteById(id);
    }

    public Map<Long, List<Map<String, LocalDate>>> getBookedPeriodsByHouse() {
        List<Rental> allRentals = rentalRepository.findAll();

        return allRentals.stream()
                .collect(Collectors.groupingBy(
                        rental -> rental.getHouse().getId(),
                        Collectors.mapping(
                                rental -> Map.of(
                                        "startDate", rental.getStartDate(),
                                        "endDate", rental.getEndDate()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    @Transactional
    public void update(Long id, House house, Client client, LocalDate startDate, LocalDate endDate) {
        Optional<Rental> optionalRental = rentalRepository.findById(id);
        if (optionalRental.isEmpty()) {
            throw new IllegalArgumentException("Аренда с id "+id+" не существует");
        }
        Rental rental = optionalRental.get();

//        if (house != null && client != null && startDate != null && endDate != null) {
//            Optional<Rental> foundByDate = rentalRepository.findByRentalDate(rental.getHouse(), rental.getClient(), startDate, endDate, rental.getId());
//            if(foundByDate.isPresent()) {
//                throw new IllegalArgumentException("Аренда с такими данными уже существует");
//            }
//        }

        if (house != null && !house.equals(rental.getHouse())) {
            rental.setHouse(house);
        }
        if (client != null && !client.equals(rental.getClient())) {
            rental.setClient(client);
        }
        if (startDate != null && !startDate.equals(rental.getStartDate())) {
            rental.setStartDate(startDate);
        }
        if (endDate != null && !endDate.equals(rental.getEndDate())) {
            rental.setEndDate(endDate);
        }
//        if (house != null || client != null && startDate != null && endDate != null){
//            Optional<Rental> foundByDate = rentalRepository.findByRentalDate(rental.getHouse().getId(), rental.getClient().getId(), startDate, endDate);
//            if(foundByDate.isPresent()) {
//                throw new IllegalStateException("Аренда с такими данными уже существует");
//            }
//            rental.setHouse(house);
//            rental.setClient(client);
//            rental.setStartDate(startDate);
//            rental.setEndDate(endDate);
//            System.out.println("Данные обновлены");
//        }
    }
}
