package com.example.guestHouse.controller.viewController;

import com.example.guestHouse.service.HouseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HouseViewController {

    private final HouseService houseService;

    public HouseViewController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping("/houses")
    public String housesPage(Model model) {
        model.addAttribute("houses", houseService.findAll());
        return "houses";
    }
}
