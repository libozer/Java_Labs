package com.postcode.repository;

import com.postcode.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    @Query("SELECT DISTINCT p FROM Person p " +
            "JOIN p.postal c " +
            "WHERE c.postcode = :name " +
            "ORDER BY p.name")
    List<Person> findAllPeopleWithPostCode(@Param("name") String genreName);
}