package com.example.guestHouse.service;

import com.example.guestHouse.repository.House;
import com.example.guestHouse.repository.HouseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class HouseService {

    private final HouseRepository houseRepository;

    public HouseService(HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }

    public List<House> findAll() {
        return houseRepository.findAllSortedByAlphabet();
    }

    public House create(House house) {
        Optional<House> optionalHouse = houseRepository.findByName(house.getName());
        if (optionalHouse.isPresent()) {
            throw new IllegalArgumentException("Дом с таким именем уже существует");
        }
        return houseRepository.save(house);
    }

    public void delete(Long id){
        Optional<House> optionalHouse = houseRepository.findById(id);
        if (optionalHouse.isEmpty()) {
            throw new IllegalArgumentException("Дома с id "+id+" не существует");
        }
        houseRepository.deleteById(id);
    }

    @Transactional
    public void update(Long id, String name, BigDecimal price) {
        Optional<House> optionalHouse = houseRepository.findById(id);
        if (optionalHouse.isEmpty()) {
            throw new IllegalArgumentException("Дома с id "+id+" не существует");
        }
        House house = optionalHouse.get();
        if (name != null && !name.equals(house.getName())){
            Optional<House> foundByName = houseRepository.findByName(name);
            if(foundByName.isPresent()) {
                throw new IllegalArgumentException("Дом с таким именем уже существует");
            }
            house.setName(name);
        }

        if (price != null && !price.equals(house.getPrice())) {
            house.setPrice(price);
        }
    }
}
