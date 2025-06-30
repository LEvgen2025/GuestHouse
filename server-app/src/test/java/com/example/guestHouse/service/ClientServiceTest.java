package com.example.guestHouse.service;

import com.example.guestHouse.repository.Client;
import com.example.guestHouse.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void findAll_ShouldReturnAllClientsSorted() {
        // Arrange
        Client client1 = new Client(1L, "Иван Иванов", "79990001122");
        Client client2 = new Client(2L, "Алексей Петров", "79990003344");
        List<Client> expectedClients = List.of(client2, client1); // Сортировка по алфавиту
        when(clientRepository.findAllSortedByAlphabet()).thenReturn(expectedClients);

        // Act
        List<Client> actualClients = clientService.findAll();

        // Assert
        assertEquals(expectedClients, actualClients);
        verify(clientRepository, times(1)).findAllSortedByAlphabet();
    }

    @Test
    void create_WithUniquePhoneNumber_ShouldSaveClient() {
        // Arrange
        Client newClient = new Client(null, "Новый Клиент", "79991112233");
        Client savedClient = new Client(1L, "Новый Клиент", "79991112233");
        when(clientRepository.findByPhoneNumber(newClient.getPhoneNumber())).thenReturn(Optional.empty());
        when(clientRepository.save(newClient)).thenReturn(savedClient);

        // Act
        Client result = clientService.create(newClient);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Новый Клиент", result.getName());
        assertEquals("79991112233", result.getPhoneNumber());
        verify(clientRepository, times(1)).findByPhoneNumber(newClient.getPhoneNumber());
        verify(clientRepository, times(1)).save(newClient);
    }

    @Test
    void create_WithExistingPhoneNumber_ShouldThrowException() {
        // Arrange
        Client existingClient = new Client(1L, "Существующий Клиент", "79992223344");
        Client newClient = new Client(null, "Новый Клиент", "79992223344");
        when(clientRepository.findByPhoneNumber(newClient.getPhoneNumber()))
                .thenReturn(Optional.of(existingClient));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> clientService.create(newClient));

        assertEquals("Клиент с таким номером телефона уже существует", exception.getMessage());
        verify(clientRepository, times(1)).findByPhoneNumber(newClient.getPhoneNumber());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void delete_WithExistingId_ShouldDeleteClient() {
        // Arrange
        Long clientId = 1L;
        Client client = new Client(clientId, "Удаляемый Клиент", "79993334455");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // Act
        clientService.delete(clientId);

        // Assert
        verify(clientRepository, times(1)).findById(clientId);
        verify(clientRepository, times(1)).deleteById(clientId);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingId = 99L;
        when(clientRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> clientService.delete(nonExistingId));

        assertEquals("Клиента с id 99 не существует", exception.getMessage());
        verify(clientRepository, times(1)).findById(nonExistingId);
        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    void update_WithValidData_ShouldUpdateClient() {
        // Arrange
        Long clientId = 1L;
        String newName = "Новое Имя";
        String newPhone = "79994445566";

        Client existingClient = new Client(clientId, "Старое Имя", "79991112233");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findByPhoneNumber(newPhone)).thenReturn(Optional.empty());

        // Act
        clientService.update(clientId, newName, newPhone);

        // Assert
        assertEquals(newName, existingClient.getName());
        assertEquals(newPhone, existingClient.getPhoneNumber());
        verify(clientRepository, times(1)).findById(clientId);
        verify(clientRepository, times(1)).findByPhoneNumber(newPhone);
    }

    @Test
    void update_WithExistingPhoneNumber_ShouldThrowException() {
        // Arrange
        Long clientId = 1L;
        String newPhone = "79995556677";

        Client existingClient = new Client(clientId, "Клиент", "79991112233");
        Client anotherClient = new Client(2L, "Другой Клиент", newPhone);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findByPhoneNumber(newPhone)).thenReturn(Optional.of(anotherClient));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> clientService.update(clientId, null, newPhone));

        assertEquals("Клиент с таким номером телефона уже существует", exception.getMessage());
        verify(clientRepository, times(1)).findById(clientId);
        verify(clientRepository, times(1)).findByPhoneNumber(newPhone);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingId = 99L;
        when(clientRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> clientService.update(nonExistingId, "Новое Имя", null));

        assertEquals("Клиента с id 99 не существует", exception.getMessage());
        verify(clientRepository, times(1)).findById(nonExistingId);
        verify(clientRepository, never()).findByPhoneNumber(any());
    }

    @Test
    void update_WithPartialData_ShouldUpdateOnlyChangedFields() {
        // Arrange
        Long clientId = 1L;
        String originalName = "Оригинальное Имя";
        String originalPhone = "79991112233";
        String newName = "Новое Имя";

        Client existingClient = new Client(clientId, originalName, originalPhone);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        // Act - обновляем только имя
        clientService.update(clientId, newName, null);

        // Assert
        assertEquals(newName, existingClient.getName());
        assertEquals(originalPhone, existingClient.getPhoneNumber());
        verify(clientRepository, times(1)).findById(clientId);
        verify(clientRepository, never()).findByPhoneNumber(any());
    }
}