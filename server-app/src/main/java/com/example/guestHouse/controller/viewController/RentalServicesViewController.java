package com.example.guestHouse.controller.viewController;

import com.example.guestHouse.service.RentalsServicesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RentalServicesViewController {

    private final RentalsServicesService rentalsServicesService;

    public RentalServicesViewController(RentalsServicesService rentalsServicesService) {
        this.rentalsServicesService = rentalsServicesService;
    }

    @GetMapping("/rentalServices")
    public String rentalServicesPage(Model model) {
        model.addAttribute("rentalServices", rentalsServicesService.findAll());
        return "rentalServices";
    }
}
