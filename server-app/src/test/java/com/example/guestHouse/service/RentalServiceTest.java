package com.example.guestHouse.service;

import com.example.guestHouse.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private HouseRepository houseRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private RentalService rentalService;

    private House createHouse(Long id, String name, BigDecimal price) {
        return new House(id, name, price);
    }

    private Client createClient(Long id, String name, String phone) {
        return new Client(id, name, phone);
    }

    private Rental createRental(Long id, House house, Client client, LocalDate startDate, LocalDate endDate, BigDecimal price) {
        return new Rental(id, house, client, startDate, endDate, price);
    }

    @Test
    void findAll_ShouldReturnAllRentalsSorted() {
        // Arrange
        House house1 = createHouse(1L, "Дом у озера", new BigDecimal("1500.00"));
        Client client1 = createClient(1L, "Иван Иванов", "79990001122");
        Rental rental1 = createRental(1L, house1, client1,
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2023, 6, 10),
                new BigDecimal("13500.00"));

        House house2 = createHouse(2L, "Апартаменты в горах", new BigDecimal("2000.00"));
        Client client2 = createClient(2L, "Петр Петров", "79990003344");
        Rental rental2 = createRental(2L, house2, client2,
                LocalDate.of(2023, 7, 1),
                LocalDate.of(2023, 7, 5),
                new BigDecimal("8000.00"));

        List<Rental> expectedRentals = List.of(rental1, rental2);
        when(rentalRepository.findAllSortedByClients()).thenReturn(expectedRentals);

        // Act
        List<Rental> actualRentals = rentalService.findAll();

        // Assert
        assertEquals(2, actualRentals.size());
        assertEquals("Иван Иванов", actualRentals.get(0).getClient().getName());
        assertEquals("Петр Петров", actualRentals.get(1).getClient().getName());
        verify(rentalRepository, times(1)).findAllSortedByClients();
    }

    @Test
    void create_WithValidHouseAndClient_ShouldSaveRental() {
        // Arrange
        House house = createHouse(1L, "Дом у озера", new BigDecimal("1500.00"));
        Client client = createClient(1L, "Иван Иванов", "79990001122");
        Rental newRental = createRental(null, house, client,
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2023, 6, 10),
                new BigDecimal("13500.00"));

        Rental savedRental = createRental(1L, house, client,
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2023, 6, 10),
                new BigDecimal("13500.00"));

        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(rentalRepository.save(newRental)).thenReturn(savedRental);

        // Act
        Rental result = rentalService.create(newRental);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(house, result.getHouse());
        assertEquals(client, result.getClient());
        verify(houseRepository, times(1)).findById(house.getId());
        verify(clientRepository, times(1)).findById(client.getId());
        verify(rentalRepository, times(1)).save(newRental);
    }

    @Test
    void create_WithNonExistingHouse_ShouldThrowException() {
        // Arrange
        Long nonExistingHouseId = 99L;
        House house = createHouse(nonExistingHouseId, "Несуществующий дом", new BigDecimal("1000.00"));
        Client client = createClient(1L, "Иван Иванов", "79990001122");
        Rental newRental = createRental(null, house, client,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                new BigDecimal("5000.00"));

        when(houseRepository.findById(nonExistingHouseId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rentalService.create(newRental));

        assertEquals("Дома с id 99 не существует", exception.getMessage());
        verify(houseRepository, times(1)).findById(nonExistingHouseId);
        verify(clientRepository, never()).findById(any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void create_WithNonExistingClient_ShouldThrowException() {
        // Arrange
        House house = createHouse(1L, "Дом у озера", new BigDecimal("1500.00"));
        Long nonExistingClientId = 99L;
        Client client = createClient(nonExistingClientId, "Несуществующий клиент", "79990001122");
        Rental newRental = createRental(null, house, client,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                new BigDecimal("5000.00"));

        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(clientRepository.findById(nonExistingClientId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rentalService.create(newRental));

        assertEquals("Клиента с id 99 не существует", exception.getMessage());
        verify(houseRepository, times(1)).findById(house.getId());
        verify(clientRepository, times(1)).findById(nonExistingClientId);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void delete_WithExistingId_ShouldDeleteRental() {
        // Arrange
        Long rentalId = 1L;
        Rental rental = createRental(rentalId,
                createHouse(1L, "Дом", new BigDecimal("1000.00")),
                createClient(1L, "Клиент", "79990001122"),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new BigDecimal("3000.00"));

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        // Act
        rentalService.delete(rentalId);

        // Assert
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(rentalRepository, times(1)).deleteById(rentalId);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingRentalId = 99L;
        when(rentalRepository.findById(nonExistingRentalId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rentalService.delete(nonExistingRentalId));

        assertEquals("Аренды с id 99 не существует", exception.getMessage());
        verify(rentalRepository, times(1)).findById(nonExistingRentalId);
        verify(rentalRepository, never()).deleteById(any());
    }

    @Test
    void getBookedPeriodsByHouse_ShouldReturnCorrectMap() {
        // Arrange
        House house1 = createHouse(1L, "Дом 1", new BigDecimal("1000.00"));
        House house2 = createHouse(2L, "Дом 2", new BigDecimal("2000.00"));

        List<Rental> rentals = List.of(
                createRental(1L, house1, createClient(1L, "Клиент 1", "79990001122"),
                        LocalDate.of(2023, 6, 1),
                        LocalDate.of(2023, 6, 10),
                        new BigDecimal("9000.00")),
                createRental(2L, house1, createClient(2L, "Клиент 2", "79990002233"),
                        LocalDate.of(2023, 7, 1),
                        LocalDate.of(2023, 7, 5),
                        new BigDecimal("4000.00")),
                createRental(3L, house2, createClient(3L, "Клиент 3", "79990003344"),
                        LocalDate.of(2023, 8, 1),
                        LocalDate.of(2023, 8, 15),
                        new BigDecimal("28000.00"))
        );

        when(rentalRepository.findAll()).thenReturn(rentals);

        // Act
        Map<Long, List<Map<String, LocalDate>>> result = rentalService.getBookedPeriodsByHouse();

        // Assert
        assertEquals(2, result.size());
        assertEquals(2, result.get(1L).size()); // 2 периода для дома 1
        assertEquals(1, result.get(2L).size()); // 1 период для дома 2

        // Проверяем первый период первого дома
        assertEquals(LocalDate.of(2023, 6, 1), result.get(1L).get(0).get("startDate"));
        assertEquals(LocalDate.of(2023, 6, 10), result.get(1L).get(0).get("endDate"));

        verify(rentalRepository, times(1)).findAll();
    }

    @Test
    void update_WithValidChanges_ShouldUpdateRental() {
        // Arrange
        Long rentalId = 1L;
        Rental existingRental = createRental(rentalId,
                createHouse(1L, "Старый дом", new BigDecimal("1000.00")),
                createClient(1L, "Старый клиент", "79990001122"),
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2023, 6, 10),
                new BigDecimal("9000.00"));

        House newHouse = createHouse(2L, "Новый дом", new BigDecimal("2000.00"));
        Client newClient = createClient(2L, "Новый клиент", "79990002233");
        LocalDate newStartDate = LocalDate.of(2023, 7, 1);
        LocalDate newEndDate = LocalDate.of(2023, 7, 10);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(existingRental));

        // Act
        rentalService.update(rentalId, newHouse, newClient, newStartDate, newEndDate);

        // Assert
        assertEquals(newHouse, existingRental.getHouse());
        assertEquals(newClient, existingRental.getClient());
        assertEquals(newStartDate, existingRental.getStartDate());
        assertEquals(newEndDate, existingRental.getEndDate());
        verify(rentalRepository, times(1)).findById(rentalId);
    }

    @Test
    void update_WithPartialChanges_ShouldUpdateOnlySpecifiedFields() {
        // Arrange
        Long rentalId = 1L;
        House originalHouse = createHouse(1L, "Оригинальный дом", new BigDecimal("1000.00"));
        Client originalClient = createClient(1L, "Оригинальный клиент", "79990001122");
        LocalDate originalStartDate = LocalDate.of(2023, 6, 1);
        LocalDate originalEndDate = LocalDate.of(2023, 6, 10);

        Rental existingRental = createRental(rentalId,
                originalHouse,
                originalClient,
                originalStartDate,
                originalEndDate,
                new BigDecimal("9000.00"));

        Client newClient = createClient(2L, "Новый клиент", "79990002233");
        LocalDate newEndDate = LocalDate.of(2023, 6, 15);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(existingRental));

        // Act - обновляем только клиента и дату окончания
        rentalService.update(rentalId, null, newClient, null, newEndDate);

        // Assert
        assertEquals(originalHouse, existingRental.getHouse()); // Не изменилось
        assertEquals(newClient, existingRental.getClient()); // Изменилось
        assertEquals(originalStartDate, existingRental.getStartDate()); // Не изменилось
        assertEquals(newEndDate, existingRental.getEndDate()); // Изменилось
        verify(rentalRepository, times(1)).findById(rentalId);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingRentalId = 99L;
        when(rentalRepository.findById(nonExistingRentalId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rentalService.update(nonExistingRentalId, null, null, null, null));

        assertEquals("Аренда с id 99 не существует", exception.getMessage());
        verify(rentalRepository, times(1)).findById(nonExistingRentalId);
    }
}