package com.example.guestHouse.service;

import com.example.guestHouse.repository.Service;
import com.example.guestHouse.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    private Service createTestService(Long id, String name, String price) {
        return new Service(id, name, new BigDecimal(price));
    }

    @Test
    void findAll_ShouldReturnAllServicesSorted() {
        // Arrange
        Service service1 = createTestService(1L, "Уборка", "1000.00");
        Service service2 = createTestService(2L, "Завтрак", "500.00");
        List<Service> expectedServices = List.of(service2, service1); // Сортировка по алфавиту
        when(serviceRepository.findAllSortedByAlphabet()).thenReturn(expectedServices);

        // Act
        List<Service> actualServices = serviceService.findAll();

        // Assert
        assertEquals(2, actualServices.size());
        assertEquals("Завтрак", actualServices.get(0).getName());
        assertEquals("Уборка", actualServices.get(1).getName());
        verify(serviceRepository, times(1)).findAllSortedByAlphabet();
    }

    @Test
    void create_WithUniqueName_ShouldSaveService() {
        // Arrange
        Service newService = createTestService(null, "Новая услуга", "1500.00");
        Service savedService = createTestService(1L, "Новая услуга", "1500.00");
        when(serviceRepository.findByName(newService.getName())).thenReturn(Optional.empty());
        when(serviceRepository.save(newService)).thenReturn(savedService);

        // Act
        Service result = serviceService.create(newService);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Новая услуга", result.getName());
        assertEquals(new BigDecimal("1500.00"), result.getPrice());
        verify(serviceRepository, times(1)).findByName(newService.getName());
        verify(serviceRepository, times(1)).save(newService);
    }

    @Test
    void create_WithExistingName_ShouldThrowException() {
        // Arrange
        Service existingService = createTestService(1L, "Существующая услуга", "1000.00");
        Service newService = createTestService(null, "Существующая услуга", "1200.00");
        when(serviceRepository.findByName(newService.getName())).thenReturn(Optional.of(existingService));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> serviceService.create(newService));

        assertEquals("Услуга с таким именем уже существует", exception.getMessage());
        verify(serviceRepository, times(1)).findByName(newService.getName());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void delete_WithExistingId_ShouldDeleteService() {
        // Arrange
        Long serviceId = 1L;
        Service service = createTestService(serviceId, "Услуга для удаления", "800.00");
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        // Act
        serviceService.delete(serviceId);

        // Assert
        verify(serviceRepository, times(1)).findById(serviceId);
        verify(serviceRepository, times(1)).deleteById(serviceId);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingId = 99L;
        when(serviceRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> serviceService.delete(nonExistingId));

        assertEquals("Услуги с id 99 не существует", exception.getMessage());
        verify(serviceRepository, times(1)).findById(nonExistingId);
        verify(serviceRepository, never()).deleteById(any());
    }

    @Test
    void update_WithValidNameAndPrice_ShouldUpdateService() {
        // Arrange
        Long serviceId = 1L;
        String newName = "Обновленное название";
        BigDecimal newPrice = new BigDecimal("2000.00");
        Service existingService = createTestService(serviceId, "Старое название", "1000.00");

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(existingService));
        when(serviceRepository.findByName(newName)).thenReturn(Optional.empty());

        // Act
        serviceService.update(serviceId, newName, newPrice);

        // Assert
        assertEquals(newName, existingService.getName());
        assertEquals(newPrice, existingService.getPrice());
        verify(serviceRepository, times(1)).findById(serviceId);
        verify(serviceRepository, times(1)).findByName(newName);
    }

    @Test
    void update_WithExistingName_ShouldThrowException() {
        // Arrange
        Long serviceId = 1L;
        String newName = "Имя которое уже есть";
        Service existingService = createTestService(serviceId, "Текущее имя", "1000.00");
        Service anotherService = createTestService(2L, newName, "2000.00");

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(existingService));
        when(serviceRepository.findByName(newName)).thenReturn(Optional.of(anotherService));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> serviceService.update(serviceId, newName, null));

        assertEquals("Услуга с таким именем уже существует", exception.getMessage());
        verify(serviceRepository, times(1)).findById(serviceId);
        verify(serviceRepository, times(1)).findByName(newName);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingId = 99L;
        when(serviceRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> serviceService.update(nonExistingId, "Новое имя", null));

        assertEquals("Услуги с id 99 не существует", exception.getMessage());
        verify(serviceRepository, times(1)).findById(nonExistingId);
        verify(serviceRepository, never()).findByName(any());
    }

    @Test
    void update_WithOnlyPriceChange_ShouldUpdatePriceOnly() {
        // Arrange
        Long serviceId = 1L;
        BigDecimal newPrice = new BigDecimal("3000.00");
        Service existingService = createTestService(serviceId, "Хорошая услуга", "2000.00");
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(existingService));

        // Act
        serviceService.update(serviceId, null, newPrice);

        // Assert
        assertEquals("Хорошая услуга", existingService.getName()); // Имя не изменилось
        assertEquals(newPrice, existingService.getPrice()); // Цена изменилась
        verify(serviceRepository, times(1)).findById(serviceId);
        verify(serviceRepository, never()).findByName(any());
    }

    @Test
    void update_WithOnlyNameChange_ShouldUpdateNameOnly() {
        // Arrange
        Long serviceId = 1L;
        String newName = "Новое крутое имя";
        Service existingService = createTestService(serviceId, "Старое имя", "1500.00");
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(existingService));
        when(serviceRepository.findByName(newName)).thenReturn(Optional.empty());

        // Act
        serviceService.update(serviceId, newName, null);

        // Assert
        assertEquals(newName, existingService.getName()); // Имя изменилось
        assertEquals(new BigDecimal("1500.00"), existingService.getPrice()); // Цена не изменилась
        verify(serviceRepository, times(1)).findById(serviceId);
        verify(serviceRepository, times(1)).findByName(newName);
    }

    @Test
    void update_WithNoChanges_ShouldDoNothing() {
        // Arrange
        Long serviceId = 1L;
        Service existingService = createTestService(serviceId, "Услуга", "1000.00");
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(existingService));

        // Act
        serviceService.update(serviceId, existingService.getName(), existingService.getPrice());

        // Assert
        assertEquals("Услуга", existingService.getName()); // Не изменилось
        assertEquals(new BigDecimal("1000.00"), existingService.getPrice()); // Не изменилось
        verify(serviceRepository, times(1)).findById(serviceId);
        verify(serviceRepository, never()).findByName(any());
    }
}