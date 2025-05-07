package com.example.guestHouse.controller.viewController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MenuViewController {

    @GetMapping("/")
    public String redirectFromRoot() {
        return "redirect:/main-page";
    }

    @GetMapping("/main-page")
    public String index(Model model) {
        return "index";
    }

}
