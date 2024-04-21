package com.postcode.testService;

import com.postcode.component.Cache;
import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.City;
import com.postcode.model.ZipCodeData;
import com.postcode.repository.CityRepository;
import com.postcode.service.CityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private Cache cache;

    @InjectMocks
    private CityService cityService;

    private City city;

    @BeforeEach
    void setUp() {
        city = new City();
        city.setId(1L);
        city.setName("Moscow");
        ZipCodeData code = new ZipCodeData(1L);
        city.setPostal(new HashSet<>(Collections.singletonList(code)));
    }

    @Test
    void testCreateCity() {
        when(cityRepository.save(city)).thenReturn(city);
        City createdCity = cityService.createCity(city);
        assertNotNull(createdCity);
        assertEquals(city, createdCity);
        verify(cityRepository, times(1)).save(city);
    }

    @Test
    void testGetCityById() {
        String cacheKey = "city-1";
        when(cache.getFromCache(cacheKey)).thenReturn(null);
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        doNothing().when(cache).addToCache(cacheKey, city);

        City fetchedCity = cityService.getCityById(1L);
        assertNotNull(fetchedCity);
        assertEquals(city, fetchedCity);
        verify(cityRepository, times(1)).findById(1);
        verify(cache, times(1)).getFromCache(cacheKey);
        verify(cache, times(1)).addToCache(cacheKey, city);
    }

    @Test
    void testGetCityByIdFromCache() {
        String cacheKey = "city-1";
        when(cache.getFromCache(cacheKey)).thenReturn(city);

        City fetchedCity = cityService.getCityById(1L);
        assertNotNull(fetchedCity);
        assertEquals(city, fetchedCity);
        verify(cityRepository, never()).findById(1);
        verify(cache, times(1)).getFromCache(cacheKey);
        verify(cache, never()).addToCache(cacheKey, city);
    }

    @Test
    void testGetAllCities() {
        List<City> cities = new ArrayList<>();
        cities.add(city);
        when(cityRepository.findAll()).thenReturn(cities);

        List<City> fetchedChains = cityService.getAllCities();
        assertNotNull(fetchedChains);
        assertEquals(1, fetchedChains.size());
        assertEquals(city, fetchedChains.get(0));
        verify(cityRepository, times(1)).findAll();
    }

    @Test
    void testUpdateCity() {
        Long cityId = 1L;
        String cacheKey = "city-1";

        City existingCity = new City();
        existingCity.setId(cityId);
        existingCity.setName("Moscow");
        existingCity.setPostal(new HashSet<>());

        City updatedCity = new City();
        updatedCity.setId(cityId);
        updatedCity.setName("Moscow 2.0");
        updatedCity.setPostal(new HashSet<>());

        when(cityRepository.findById(Math.toIntExact(cityId))).thenReturn(Optional.of(existingCity));
        when(cityRepository.save(any(City.class))).thenReturn(updatedCity);
        doNothing().when(cache).removeFromCache(cacheKey);
        doNothing().when(cache).addToCache(any(String.class), any(City.class));

        City returnedCity = cityService.updateCity(cityId, updatedCity);

        assertNotNull(returnedCity);
        assertEquals("Moscow 2.0", returnedCity.getName());
        verify(cityRepository, times(1)).findById(Math.toIntExact(cityId));
        verify(cityRepository, times(1)).save(any(City.class));
        verify(cache, times(1)).removeFromCache(cacheKey);
        verify(cache, times(1)).addToCache(any(String.class), any(City.class));
    }

    @Test
    void testDeleteCity() {
        String cacheKey = "city-1";

        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        doNothing().when(cache).removeFromCache(cacheKey);
        doNothing().when(cityRepository).deleteById(1);

        cityService.deleteCity(1L);

        verify(cityRepository, times(1)).findById(1);
        verify(cache, times(1)).removeFromCache(cacheKey);
        verify(cityRepository, times(1)).deleteById(1);
    }

    @Test
    void testGetCityByIdNotFound() {
        when(cityRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> cityService.getCityById(1L));
        verify(cityRepository, times(1)).findById(1);
    }

    @Test
    void testUpdateCityNotFound() {
        when(cityRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> cityService.updateCity(1L, new City()));
        verify(cityRepository, times(1)).findById(1);
    }

    @Test
    void testDeleteCityNotFound() {
        when(cityRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> cityService.deleteCity(1L));
        verify(cityRepository, times(1)).findById(1);
    }
}