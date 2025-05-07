package com.example.guestHouse.controller.apiController;

import com.example.guestHouse.repository.Client;
import com.example.guestHouse.service.ClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/show")
    public List<Client> findAll() {
        return clientService.findAll();
    }

    @PostMapping
    public Client create(@RequestBody Client client) {
        return clientService.create(client);
    }

    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable Long id){
        clientService.delete(id);
    }

    @PutMapping(path = "{id}")
    public void update(@PathVariable Long id,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String phoneNumber
    ){
        clientService.update(id, name, phoneNumber);
    }
}
