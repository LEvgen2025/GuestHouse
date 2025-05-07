package com.example.guestHouse.controller.apiController;

import com.example.guestHouse.repository.Service;
import com.example.guestHouse.service.ServiceService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(path = "api/services")
public class ServiceController {

    private final ServiceService serviceService;

    private ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping("/show")
    public List<Service> findAll() {
        return serviceService.findAll();
    }

    @PostMapping
    public Service create(@RequestBody Service service) { return serviceService.create(service); }

    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable Long id) {
        serviceService.delete(id);
    }

    @PutMapping(path = "{id}")
    public void update(@PathVariable Long id,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) BigDecimal price
    ){
        serviceService.update(id, name, price);
    }
}
