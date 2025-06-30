package com.example.guestHouse.service;

import com.example.guestHouse.repository.House;
import com.example.guestHouse.repository.HouseRepository;
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
class HouseServiceTest {

    @Mock
    private HouseRepository houseRepository;

    @InjectMocks
    private HouseService houseService;

    private House createTestHouse(Long id, String name, String price) {
        return new House(id, name, new BigDecimal(price));
    }

    @Test
    void findAll_ShouldReturnAllHousesSorted() {
        // Arrange
        House house1 = createTestHouse(1L, "Дом у озера", "1500.00");
        House house2 = createTestHouse(2L, "Апартаменты в горах", "2000.00");
        List<House> expectedHouses = List.of(house2, house1); // Сортировка по алфавиту
        when(houseRepository.findAllSortedByAlphabet()).thenReturn(expectedHouses);

        // Act
        List<House> actualHouses = houseService.findAll();

        // Assert
        assertEquals(2, actualHouses.size());
        assertEquals("Апартаменты в горах", actualHouses.get(0).getName());
        assertEquals("Дом у озера", actualHouses.get(1).getName());
        verify(houseRepository, times(1)).findAllSortedByAlphabet();
    }

    @Test
    void create_WithUniqueName_ShouldSaveHouse() {
        // Arrange
        House newHouse = createTestHouse(null, "Новый дом", "1000.00");
        House savedHouse = createTestHouse(1L, "Новый дом", "1000.00");
        when(houseRepository.findByName(newHouse.getName())).thenReturn(Optional.empty());
        when(houseRepository.save(newHouse)).thenReturn(savedHouse);

        // Act
        House result = houseService.create(newHouse);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Новый дом", result.getName());
        assertEquals(new BigDecimal("1000.00"), result.getPrice());
        verify(houseRepository, times(1)).findByName(newHouse.getName());
        verify(houseRepository, times(1)).save(newHouse);
    }

    @Test
    void create_WithExistingName_ShouldThrowException() {
        // Arrange
        House existingHouse = createTestHouse(1L, "Существующий дом", "1200.00");
        House newHouse = createTestHouse(null, "Существующий дом", "1300.00");
        when(houseRepository.findByName(newHouse.getName())).thenReturn(Optional.of(existingHouse));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> houseService.create(newHouse));

        assertEquals("Дом с таким именем уже существует", exception.getMessage());
        verify(houseRepository, times(1)).findByName(newHouse.getName());
        verify(houseRepository, never()).save(any());
    }

    @Test
    void delete_WithExistingId_ShouldDeleteHouse() {
        // Arrange
        Long houseId = 1L;
        House house = createTestHouse(houseId, "Дом для удаления", "900.00");
        when(houseRepository.findById(houseId)).thenReturn(Optional.of(house));

        // Act
        houseService.delete(houseId);

        // Assert
        verify(houseRepository, times(1)).findById(houseId);
        verify(houseRepository, times(1)).deleteById(houseId);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingId = 99L;
        when(houseRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> houseService.delete(nonExistingId));

        assertEquals("Дома с id 99 не существует", exception.getMessage());
        verify(houseRepository, times(1)).findById(nonExistingId);
        verify(houseRepository, never()).deleteById(any());
    }

    @Test
    void update_WithValidNameAndPrice_ShouldUpdateHouse() {
        // Arrange
        Long houseId = 1L;
        String newName = "Обновленное название";
        BigDecimal newPrice = new BigDecimal("2500.00");
        House existingHouse = createTestHouse(houseId, "Старое название", "1500.00");

        when(houseRepository.findById(houseId)).thenReturn(Optional.of(existingHouse));
        when(houseRepository.findByName(newName)).thenReturn(Optional.empty());

        // Act
        houseService.update(houseId, newName, newPrice);

        // Assert
        assertEquals(newName, existingHouse.getName());
        assertEquals(newPrice, existingHouse.getPrice());
        verify(houseRepository, times(1)).findById(houseId);
        verify(houseRepository, times(1)).findByName(newName);
    }

    @Test
    void update_WithExistingName_ShouldThrowException() {
        // Arrange
        Long houseId = 1L;
        String newName = "Имя которое уже есть";
        House existingHouse = createTestHouse(houseId, "Текущее имя", "1000.00");
        House anotherHouse = createTestHouse(2L, newName, "2000.00");

        when(houseRepository.findById(houseId)).thenReturn(Optional.of(existingHouse));
        when(houseRepository.findByName(newName)).thenReturn(Optional.of(anotherHouse));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> houseService.update(houseId, newName, null));

        assertEquals("Дом с таким именем уже существует", exception.getMessage());
        verify(houseRepository, times(1)).findById(houseId);
        verify(houseRepository, times(1)).findByName(newName);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long nonExistingId = 99L;
        when(houseRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> houseService.update(nonExistingId, "Новое имя", null));

        assertEquals("Дома с id 99 не существует", exception.getMessage());
        verify(houseRepository, times(1)).findById(nonExistingId);
        verify(houseRepository, never()).findByName(any());
    }

    @Test
    void update_WithOnlyPriceChange_ShouldUpdatePriceOnly() {
        // Arrange
        Long houseId = 1L;
        BigDecimal newPrice = new BigDecimal("3000.00");
        House existingHouse = createTestHouse(houseId, "Хороший дом", "2000.00");
        when(houseRepository.findById(houseId)).thenReturn(Optional.of(existingHouse));

        // Act
        houseService.update(houseId, null, newPrice);

        // Assert
        assertEquals("Хороший дом", existingHouse.getName()); // Имя не изменилось
        assertEquals(newPrice, existingHouse.getPrice()); // Цена изменилась
        verify(houseRepository, times(1)).findById(houseId);
        verify(houseRepository, never()).findByName(any());
    }

    @Test
    void update_WithOnlyNameChange_ShouldUpdateNameOnly() {
        // Arrange
        Long houseId = 1L;
        String newName = "Новое крутое имя";
        House existingHouse = createTestHouse(houseId, "Старое имя", "1500.00");
        when(houseRepository.findById(houseId)).thenReturn(Optional.of(existingHouse));
        when(houseRepository.findByName(newName)).thenReturn(Optional.empty());

        // Act
        houseService.update(houseId, newName, null);

        // Assert
        assertEquals(newName, existingHouse.getName()); // Имя изменилось
        assertEquals(new BigDecimal("1500.00"), existingHouse.getPrice()); // Цена не изменилась
        verify(houseRepository, times(1)).findById(houseId);
        verify(houseRepository, times(1)).findByName(newName);
    }
}