package com.example.guestHouse.controller.viewController;

import com.example.guestHouse.service.ServiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServiceViewController {
    private final ServiceService serviceService;

    public ServiceViewController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping("/services")
    public String servicesPage(Model model) {
        model.addAttribute("services", serviceService.findAll());
        return "services";
    }
}
