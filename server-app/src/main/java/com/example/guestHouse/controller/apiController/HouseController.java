package com.example.guestHouse.controller.apiController;

import com.example.guestHouse.repository.House;
import com.example.guestHouse.service.HouseService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(path = "api/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping("/show")
    public List<House> findAll() {
        return houseService.findAll();
    }

    @PostMapping
    public House create(@RequestBody House house) { return houseService.create(house); }

    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable Long id) {
        houseService.delete(id);
    }

    @PutMapping(path = "{id}")
    public void update(@PathVariable Long id,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) BigDecimal price
    ){
        houseService.update(id, name, price);
    }
}