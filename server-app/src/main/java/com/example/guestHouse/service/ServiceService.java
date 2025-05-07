package com.example.guestHouse.service;

import com.example.guestHouse.repository.Service;
import com.example.guestHouse.repository.ServiceRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<Service> findAll() {
        return serviceRepository.findAllSortedByAlphabet();
    }

    public Service create(Service service) {
        Optional<Service> optionalService = serviceRepository.findByName(service.getName());
        if (optionalService.isPresent()) {
            throw new IllegalArgumentException("Услуга с таким именем уже существует");
        }
        return serviceRepository.save(service);
    }

    public void delete(Long id){
        Optional<Service> optionalService = serviceRepository.findById(id);
        if (optionalService.isEmpty()) {
            throw new IllegalArgumentException("Услуги с id "+id+" не существует");
        }
        serviceRepository.deleteById(id);
    }

    @Transactional
    public void update(Long id, String name, BigDecimal price) {
        Optional<Service> optionalService = serviceRepository.findById(id);
        if (optionalService.isEmpty()) {
            throw new IllegalArgumentException("Услуги с id "+id+" не существует");
        }
        Service service = optionalService.get();
        if (name != null && !name.equals(service.getName())){
            Optional<Service> foundByName = serviceRepository.findByName(name);
            if(foundByName.isPresent()) {
                throw new IllegalArgumentException("Услуга с таким именем уже существует");
            }
            service.setName(name);
        }

        if (price != null && !price.equals(service.getPrice())) {
            service.setPrice(price);
        }
    }
}
