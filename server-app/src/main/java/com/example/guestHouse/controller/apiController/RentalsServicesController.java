package com.example.guestHouse.controller.apiController;

import com.example.guestHouse.repository.*;
import com.example.guestHouse.service.RentalsServicesService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "api/rentServices")
public class RentalsServicesController {

    private final RentalsServicesService rentalsServicesService;

    public RentalsServicesController(RentalsServicesService rentalsServicesService) {
        this.rentalsServicesService = rentalsServicesService;
    }

    @GetMapping("/show")
    public List<RentalsServices> findAll() {
        return rentalsServicesService.findAll();
    }

    @PostMapping
    public RentalsServices create(@RequestBody RentalsServices rentalsServices) {
        return rentalsServicesService.create(rentalsServices);
    }

    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable Long id) {
        rentalsServicesService.delete(id);
    }

    @PutMapping(path = "{id}")
    public void update(@PathVariable Long id,
                       @RequestParam(required = false) Service service,
                       @RequestParam(required = false) Rental rental,
                       @RequestParam(required = false) LocalDateTime exTime
    ){
        rentalsServicesService.update(id, service, rental, exTime);
    }
}
