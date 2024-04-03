package com.postcode.controller;

import com.postcode.model.ZipCodeData;
import com.postcode.model.Person;
import com.postcode.service.PostCodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.postcode.service.PersonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/person")
public class PersonController {
    private final PersonService personService;
    private final PostCodeService postCodeService;

    @PostMapping
    public ResponseEntity<Person> create(@RequestBody Person person){
        Person savedPerson = personService.createPerson(person);
        return new ResponseEntity<>(savedPerson, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public Person getPersonById(@PathVariable("id") Long personId){
        return personService.getPersonById(personId);
    }

    @GetMapping
    public ResponseEntity<List<Person>> getAllPeople(){
        List<Person> people = personService.getAllPeople();
        return ResponseEntity.ok(people);
    }

    @GetMapping("/with/{postcode}")
    public ResponseEntity<List<Person>> getAllSterlingMovies(@PathVariable("postcode") String postal) {
        List<Person> people = personService.getAllPeopleWithPostCode(postal);
        return ResponseEntity.ok(people);
    }

    @PutMapping("{id}")
    public Person updatePerson(@PathVariable("id") Long personId, @RequestBody Person updatedPerson){
        return personService.updatePerson(personId, updatedPerson);
    }

    @PutMapping("/{person_id}/post/{post_id}")
    Person addCryptoToPerson(@PathVariable("person_id") Long personId, @PathVariable("post_id") Long cryptoId){
        Person person = personService.getPersonById(personId);
        ZipCodeData zipCodeData = postCodeService.getPostCodeDataById(cryptoId);
        person.addCrypto(zipCodeData);
        return personService.updatePerson(personId, person);
    }

    @DeleteMapping("/{person_id}")
    public ResponseEntity<String> deletePerson(@PathVariable("person_id") Long personId) {
        personService.deletePerson(personId);
        return ResponseEntity.ok("Person deleted successfully");
    }
}