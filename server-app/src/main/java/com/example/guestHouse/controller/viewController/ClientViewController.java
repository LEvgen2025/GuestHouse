package com.example.guestHouse.controller.viewController;

import com.example.guestHouse.service.ClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ClientViewController {

    private final ClientService clientService;

    public ClientViewController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/clients")
    public String clientsPage(Model model) {
        model.addAttribute("clients", clientService.findAll());
        return "clients"; // возвращает clients.html
    }

}
