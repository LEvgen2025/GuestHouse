package com.example.guestHouse.service;

import com.example.guestHouse.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RentalsServicesService {
    private final RentalsServicesRepository rentalsServicesRepository;
    private final ServiceRepository serviceRepository;
    private final RentalRepository rentalRepository;

    public RentalsServicesService(RentalsServicesRepository rentalsServicesRepository, ServiceRepository serviceRepository, RentalRepository rentalRepository) {
        this.rentalsServicesRepository = rentalsServicesRepository;
        this.serviceRepository = serviceRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<RentalsServices> findAll() {
        return rentalsServicesRepository.findAll();
    }

    public RentalsServices create(RentalsServices rentalsServices) {
        Optional<com.example.guestHouse.repository.Service> optionalService = serviceRepository.findById(rentalsServices.getService().getId());
        if (optionalService.isEmpty()) {
            throw new IllegalStateException("Услуги с id "+rentalsServices.getService().getId()+" не существует");
        }
        Optional<Rental> optionalRental = rentalRepository.findById(rentalsServices.getRental().getId());
        if (optionalRental.isEmpty()) {
            throw new IllegalStateException("Аренды с id "+rentalsServices.getRental().getId()+" не существует");
        }

        //Возможно следует добавить проверку на наличие записи с такими же данными

        return rentalsServicesRepository.save(rentalsServices);
    }

    public void delete(Long id){
        Optional<RentalsServices> optionalRentalsServices = rentalsServicesRepository.findById(id);
        if (optionalRentalsServices.isEmpty()) {
            throw new IllegalArgumentException("Заказанной услуги с id "+id+" не существует");
        }

        rentalsServicesRepository.deleteById(id);
    }

    @Transactional
    public void update(Long id, com.example.guestHouse.repository.Service service, Rental rental, LocalDateTime exTime) {
        Optional<RentalsServices> optionalRentalsServices = rentalsServicesRepository.findById(id);
        if (optionalRentalsServices.isEmpty()) {
            throw new IllegalArgumentException("Заказанной услуги с id "+id+" не существует");
        }
        RentalsServices rentalsServices = optionalRentalsServices.get();

        //Возможно следует добавить проверку на наличие записи с такими же данными

        if (service != null && !service.equals(rentalsServices.getService())) {
            rentalsServices.setService(service);
        }
        if (rental != null && !rental.equals(rentalsServices.getRental())) {
            rentalsServices.setRental(rental);
        }
        if (exTime != null && !exTime.equals(rentalsServices.getExTime())) {
            rentalsServices.setExTime(exTime);
        }
    }
}
