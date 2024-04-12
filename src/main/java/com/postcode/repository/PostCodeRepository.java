package com.postcode.repository;

import com.postcode.model.ZipCodeData;
import com.postcode.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PostCodeRepository extends JpaRepository<ZipCodeData, Integer> {
    ZipCodeData findByPostcode(@Param("name") String name);

    void deleteByPersonsContains(Person person);
}