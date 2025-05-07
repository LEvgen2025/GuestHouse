package com.example.guestHouse.controller.viewController;

import com.example.guestHouse.service.RentalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class RentalViewController {

    private final RentalService rentalService;

    public RentalViewController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("/rentals")
    public String rentalsPage(Model model) {
        model.addAttribute("rentals", rentalService.findAll());
        // Получаем список занятых периодов в формате {houseId: [{startDate, endDate}, ...]}
        Map<Long, List<Map<String, LocalDate>>> bookedPeriods = rentalService.getBookedPeriodsByHouse();
        model.addAttribute("bookedPeriods", bookedPeriods);
        System.out.println(bookedPeriods);
        return "rentals";
    }
}
