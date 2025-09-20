package com.example.guestHouse.controller.apiController;

import com.example.guestHouse.repository.Client;
import com.example.guestHouse.repository.House;
import com.example.guestHouse.repository.Rental;
import com.example.guestHouse.service.RentalService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("/show")
    public List<Rental> findAll() {
        return rentalService.findAll();
    }

    @PostMapping
    public Rental create(@RequestBody Rental rental) {
        return rentalService.create(rental);
    }

    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable Long id) {
        rentalService.delete(id);
    }

    @PutMapping(path = "{id}")
    public void update(@PathVariable Long id,
                       @RequestParam(required = false) House house,
                       @RequestParam(required = false) Client client,
                       @RequestParam(required = false) LocalDate startDate,
                       @RequestParam(required = false) LocalDate endDate
    ){
        rentalService.update(id, house, client, startDate, endDate);
    }

    @PutMapping("/discount")
    public void setDiscount(@RequestParam Long id, @RequestParam BigDecimal value) {
        rentalService.set_discount(value, id);
    }
}
