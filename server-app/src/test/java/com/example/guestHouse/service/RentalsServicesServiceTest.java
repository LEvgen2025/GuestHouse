package com.example.guestHouse.service;

import com.example.guestHouse.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalsServicesServiceTest {

    @Mock
    private RentalsServicesRepository rentalsServicesRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private RentalsServicesService rentalsServicesService;

    private com.example.guestHouse.repository.Service createService(Long id, String name, BigDecimal price) {
        return new com.example.guestHouse.repository.Service(id, name, price);
    }

    private Rental createRental(Long id, House house, Client client,
                                LocalDate startDate, LocalDate endDate, BigDecimal price) {
        return new Rental(id, house, client, startDate, endDate, price);
    }

    private RentalsServices createRentalsServices(Long id,
                                                  com.example.guestHouse.repository.Service service,
                                                  Rental rental,
                                                  LocalDateTime exTime) {
        return new RentalsServices(id, service, rental, exTime);
    }

    @Test
    void findAll_ShouldReturnAllRentalsServices() {
        // Arrange
        com.example.guestHouse.repository.Service service = createService(1L, "Уборка", new BigDecimal("1000.00"));
        Rental rental = createRental(1L,
                new House(1L, "Дом", new BigDecimal("5000.00")),
                new Client(1L, "Клиент", "79990001122"),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new BigDecimal("15000.00"));

        RentalsServices rs1 = createRentalsServices(1L, service, rental, LocalDateTime.now());
        RentalsServices rs2 = createRentalsServices(2L, service, rental, LocalDateTime.now().plusHours(2));

        List<RentalsServices> expectedList = List.of(rs1, rs2);
        when(rentalsServicesRepository.findAll()).thenReturn(expectedList);

        // Act
        List<RentalsServices> result = rentalsServicesService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(rs1, result.get(0));
        assertEquals(rs2, result.get(1));
        verify(rentalsServicesRepository, times(1)).findAll();
    }

    @Test
    void create_WithValidServiceAndRental_ShouldSaveRentalsServices() {
        // Arrange
        com.example.guestHouse.repository.Service service = createService(1L, "Завтрак", new BigDecimal("500.00"));
        Rental rental = createRental(1L,
                new House(1L, "Дом", new BigDecimal("5000.00")),
                new Client(1L, "Клиент", "79990001122"),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new BigDecimal("15000.00"));

        LocalDateTime exTime = LocalDateTime.now().plusHours(1);
        RentalsServices newRentalsServices = createRentalsServices(null, service, rental, exTime);
        RentalsServices savedRentalsServices = createRentalsServices(1L, service, rental, exTime);

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(rentalsServicesRepository.save(newRentalsServices)).thenReturn(savedRentalsServices);

        // Act
        RentalsServices result = rentalsServicesService.create(newRentalsServices);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(service, result.getService());
        assertEquals(rental, result.getRental());
        assertEquals(exTime, result.getExTime());
        verify(serviceRepository, times(1)).findById(service.getId());
        verify(rentalRepository, times(1)).findById(rental.getId());
        verify(rentalsServicesRepository, times(1)).save(newRentalsServices);
    }

    @Test
    void create_WithNonExistingService_ShouldThrowException() {
        // Arrange
        Long nonExistingServiceId = 99L;
        com.example.guestHouse.repository.Service service = createService(nonExistingServiceId, "Несуществующая услуга", new BigDecimal("1000.00"));
        Rental rental = createRental(1L,
                new House(1L, "Дом", new BigDecimal("5000.00")),
                new Client(1L, "Клиент", "79990001122"),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new BigDecimal("15000.00"));

        RentalsServices newRentalsServices = createRentalsServices(null, service, rental, LocalDateTime.now());

        when(serviceRepository.findById(nonExistingServiceId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rentalsServicesService.create(newRentalsServices));

        assertEquals("Услуги с id 99 не существует", exception.getMessage());
        verify(serviceRepository, times(1)).findById(nonExistingServiceId);
        verify(rentalRepository, never()).findById(any());
        verify(rentalsServicesRepository, never()).save(any());
    }

    @Test
    void create_WithNonExistingRental_ShouldThrowException() {
        // Arrange
        com.example.guestHouse.repository.Service service = createService(1L, "Ужин", new BigDecimal("800.00"));
        Long nonExistingRentalId = 99L;
        Rental rental = createRental(nonExistingRentalId,
                new House(1L, "Дом", new BigDecimal("5000.00")),
                new Client(1L, "Клиент", "79990001122"),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new BigDecimal("15000.00"));

        RentalsServices newRentalsServices = createRentalsServices(null, service, rental, LocalDateTime.now());

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(rentalRepository.findById(nonExistingRentalId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rentalsServicesService.create(newRentalsServices));

        assertEquals("Аренды с id 99 не существует", exception.getMessage());
        verify(serviceRepository, times(1)).findById(service.getId());
        verify(rentalRepository, times(1)).findById(nonExistingRentalId);
        verify(rentalsServicesRepository, never()).save(any());
    }

    @Test
    void delete_WithExistingId_ShouldDeleteRentalsServices() {
        // Arrange
        Long rentalsServicesId = 1L;
        RentalsServices rentalsServices = createRentalsServices(rentalsServicesId,
                createService(1L, "Уборка", new BigDecimal("1000.00")),
                createRental(1L,
                        new House(1L, "Дом", new BigDecimal("5000.00")),
                        new Client(1L, "Клиент", "79990001122"),
                        LocalDate.now(),
                        LocalDate.now().plusDays(3),
                        new BigDecimal("15000.00")),
                LocalDateTime.now());

        when(rentalsServicesRepository.findById(rentalsServicesId)).thenReturn(Optional.of(rentalsServices));

        // Act
        rentalsServicesService.delete(rentalsServicesId);

        // Assert
        verify(rentalsServicesRepository, times(1)).findById(rentalsServicesId);
        verify(rentalsServicesRepository, times(1)).deleteById(rentalsServicesId);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingId = 99L;
        when(rentalsServicesRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rentalsServicesService.delete(nonExistingId));

        assertEquals("Заказанной услуги с id 99 не существует", exception.getMessage());
        verify(rentalsServicesRepository, times(1)).findById(nonExistingId);
        verify(rentalsServicesRepository, never()).deleteById(any());
    }

    @Test
    void update_WithAllChanges_ShouldUpdateAllFields() {
        // Arrange
        Long rentalsServicesId = 1L;
        RentalsServices existing = createRentalsServices(rentalsServicesId,
                createService(1L, "Старая услуга", new BigDecimal("500.00")),
                createRental(1L,
                        new House(1L, "Старый дом", new BigDecimal("5000.00")),
                        new Client(1L, "Старый клиент", "79990001122"),
                        LocalDate.now(),
                        LocalDate.now().plusDays(3),
                        new BigDecimal("15000.00")),
                LocalDateTime.now());

        com.example.guestHouse.repository.Service newService = createService(2L, "Новая услуга", new BigDecimal("1000.00"));
        Rental newRental = createRental(2L,
                new House(2L, "Новый дом", new BigDecimal("7000.00")),
                new Client(2L, "Новый клиент", "79990002233"),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(8),
                new BigDecimal("21000.00"));
        LocalDateTime newExTime = LocalDateTime.now().plusHours(2);

        when(rentalsServicesRepository.findById(rentalsServicesId)).thenReturn(Optional.of(existing));

        // Act
        rentalsServicesService.update(rentalsServicesId, newService, newRental, newExTime);

        // Assert
        assertEquals(newService, existing.getService());
        assertEquals(newRental, existing.getRental());
        assertEquals(newExTime, existing.getExTime());
        verify(rentalsServicesRepository, times(1)).findById(rentalsServicesId);
    }

    @Test
    void update_WithPartialChanges_ShouldUpdateOnlySpecifiedFields() {
        // Arrange
        Long rentalsServicesId = 1L;
        com.example.guestHouse.repository.Service originalService = createService(1L, "Оригинальная услуга", new BigDecimal("500.00"));
        Rental originalRental = createRental(1L,
                new House(1L, "Оригинальный дом", new BigDecimal("5000.00")),
                new Client(1L, "Оригинальный клиент", "79990001122"),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new BigDecimal("15000.00"));
        LocalDateTime originalExTime = LocalDateTime.now();

        RentalsServices existing = createRentalsServices(rentalsServicesId,
                originalService,
                originalRental,
                originalExTime);

        LocalDateTime newExTime = LocalDateTime.now().plusHours(3);

        when(rentalsServicesRepository.findById(rentalsServicesId)).thenReturn(Optional.of(existing));

        // Act - обновляем только время выполнения
        rentalsServicesService.update(rentalsServicesId, null, null, newExTime);

        // Assert
        assertEquals(originalService, existing.getService()); // Не изменилось
        assertEquals(originalRental, existing.getRental()); // Не изменилось
        assertEquals(newExTime, existing.getExTime()); // Изменилось
        verify(rentalsServicesRepository, times(1)).findById(rentalsServicesId);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingId = 99L;
        when(rentalsServicesRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rentalsServicesService.update(nonExistingId, null, null, null));

        assertEquals("Заказанной услуги с id 99 не существует", exception.getMessage());
        verify(rentalsServicesRepository, times(1)).findById(nonExistingId);
    }
}