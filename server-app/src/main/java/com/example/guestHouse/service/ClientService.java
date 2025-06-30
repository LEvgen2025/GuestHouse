package com.example.guestHouse.service;

import com.example.guestHouse.repository.Client;
import com.example.guestHouse.repository.ClientRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAllSortedByAlphabet();
    }

    public Client create(Client client) {
        Optional<Client> optionalClient = clientRepository.findByPhoneNumber(client.getPhoneNumber());
        if(optionalClient.isPresent()) {
            throw new IllegalArgumentException("Клиент с таким номером телефона уже существует");
        }
        return clientRepository.save(client);
    }

    public void delete(Long id){
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isEmpty()) {
           throw new IllegalArgumentException("Клиента с id "+id+" не существует");
        }
        clientRepository.deleteById(id);
    }

    @Transactional
    public void update(Long id, String name, String phoneNumber) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isEmpty()) {
            throw new IllegalArgumentException("Клиента с id "+id+" не существует");
        }
        Client client = optionalClient.get();
        if (phoneNumber != null && !phoneNumber.equals(client.getPhoneNumber())){
            Optional<Client> foundByPhoneNumber = clientRepository.findByPhoneNumber(phoneNumber);
            if(foundByPhoneNumber.isPresent()) {
                throw new IllegalArgumentException("Клиент с таким номером телефона уже существует");
            }
            client.setPhoneNumber(phoneNumber);
        }

        if (name != null && !name.equals(client.getName())) {
            client.setName(name);
        }
    }
}
