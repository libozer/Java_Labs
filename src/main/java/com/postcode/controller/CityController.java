package com.postcode.controller;

import com.postcode.model.ZipCodeData;
import com.postcode.model.City;
import com.postcode.service.CityService;
import com.postcode.service.PostCodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/city")
public class CityController {
    private final CityService cityService;
    private final PostCodeService coinCapService;

    @PostMapping
    public ResponseEntity<City> create(@RequestBody City city){
        City savedCity = cityService.createCity(city);
        return new ResponseEntity<>(savedCity, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public City getChainById(@PathVariable("id") Long cityId){
        return cityService.getCityById(cityId);
    }

    @GetMapping
    public ResponseEntity<List<City>> getAllCities(){
        List<City> cities = cityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @PutMapping("{id}")
    public City updateChain(@PathVariable("id") Long cityId, @RequestBody City updatedCity){
        return cityService.updateCity(cityId, updatedCity);
    }

    @PutMapping("/{city_id}/postcode/{post_id}")
    City addCryptoToCity(@PathVariable("city_id") Long cityId, @PathVariable("post_id") Long postId){
        City city = cityService.getCityById(cityId);
        ZipCodeData zipCodeData = coinCapService.getPostCodeDataById(postId);
        city.addCrypto(zipCodeData);
        zipCodeData.setCity(city);
        coinCapService.updatePostCodeData(postId, zipCodeData);
        return cityService.updateCity(cityId, city);
    }

    @DeleteMapping("/{city_id}")
    public ResponseEntity<String> deleteCity(@PathVariable("city_id") Long cityId){
        cityService.deleteCity(cityId);
        return ResponseEntity.ok("City deleted successfully");
    }

}