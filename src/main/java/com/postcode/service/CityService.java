package com.postcode.service;

import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.City;
import com.postcode.model.ZipCodeData;
import com.postcode.repository.CityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CityService {
    private final CityRepository cityRepository;
    private static final String ERROR_MESSAGE = "City does not exist with given id: ";

    public City createCity(City city) {
        return cityRepository.save(city);
    }

    public City getCityById(Long cityId) {
        return cityRepository.findById(Math.toIntExact(cityId))
                .orElseThrow(()->
                        new EntityNotFoundException(ERROR_MESSAGE + cityId));
    }


    public List<City> getAllCities() {
        return cityRepository.findAll();
    }


    public City updateCity(Long cityId, City updatedCity) {
        City city = cityRepository.findById(Math.toIntExact(cityId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + cityId)
        );

        city.setName(updatedCity.getName());
        city.setPostal(updatedCity.getPostal());

        return cityRepository.save(city);
    }


    public void deleteCity(Long cityId) {
        City city = cityRepository.findById(Math.toIntExact(cityId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + cityId)
        );

        if (city != null){
            for (ZipCodeData zipCodeData : city.getPostal()) {
                zipCodeData.setCity(null);
            }

            city.getPostal().clear();
            cityRepository.deleteById(Math.toIntExact(cityId));
        }
    }
}