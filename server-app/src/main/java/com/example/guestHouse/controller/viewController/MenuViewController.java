package com.example.guestHouse.controller.viewController;

import com.example.guestHouse.service.RentalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class MenuViewController {

    private final RentalService rentalService;

    public MenuViewController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("/")
    public String redirectFromRoot() {
        return "redirect:/main-page";
    }

    @GetMapping("/main-page")
    public String index(Model model) {
        // Получаем список занятых периодов в формате {houseId: [{startDate, endDate}, ...]}
        Map<Long, List<Map<String, LocalDate>>> bookedPeriods = rentalService.getBookedPeriodsByHouse();
        model.addAttribute("bookedPeriods", bookedPeriods);
        return "index";
    }

}
