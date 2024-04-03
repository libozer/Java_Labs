package com.postcode.service;
import com.postcode.component.Cache;
import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.City;
import com.postcode.model.ZipCodeData;
import com.postcode.repository.CityRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CityService {
    private final CityRepository cityRepository;
    private final Cache cache;
    private static final Logger log = LoggerFactory.getLogger(CityService.class);
    private static final String ERROR_MESSAGE = "City does not exist with given id: ";
    private static final String CACHE_KEY = "city-";
    private static final String CACHE_HIT = "Cash HIT using key: %s";
    private static final String CACHE_MISS = "Cash MISS using key: %s";

    public City createCity(City city) {
        return cityRepository.save(city);
    }

    public City getCityById(Long cityId) {
        String cacheKey = CACHE_KEY + cityId;
        City cachedCity = (City) cache.getFromCache(cacheKey);
        if (cachedCity != null){
            String logstash = String.format(CACHE_HIT, cacheKey);
            log.info(logstash);
            return cachedCity;
        }
        String logstash = String.format(CACHE_MISS, cacheKey);
        log.info(logstash);
        City chainFromRepo = cityRepository.findById(Math.toIntExact(cityId))
                .orElseThrow(()-> new EntityNotFoundException(ERROR_MESSAGE + cityId));
        cache.addToCache(cacheKey, chainFromRepo);
        return chainFromRepo;
    }


    public List<City> getAllCities() {
        return cityRepository.findAll();
    }


    public City updateCity(Long cityId, City updatedCity) {
        City city = cityRepository.findById(Math.toIntExact(cityId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + cityId)
        );
        String cacheKey = CACHE_KEY + city.getId();
        cache.removeFromCache(cacheKey);
        city.setName(updatedCity.getName());
        city.setPostal(updatedCity.getPostal());
        cache.addToCache(cacheKey, city);
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

            String cacheKey = CACHE_KEY + city.getId();
            cache.removeFromCache(cacheKey);
            city.getPostal().clear();
            cityRepository.deleteById(Math.toIntExact(cityId));
        }
    }
}