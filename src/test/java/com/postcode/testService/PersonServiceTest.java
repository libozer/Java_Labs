package com.postcode.testService;

import com.postcode.component.Cache;
import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.ZipCodeData;
import com.postcode.model.Person;
import com.postcode.repository.PersonRepository;
import com.postcode.service.PersonService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private Cache cache;

    @InjectMocks
    private PersonService personService;

    private Person person;
    private ZipCodeData postcode;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1L);
        person.setName("John Doe");
        postcode = new ZipCodeData(1L);
        postcode.setPostcode("m320jg");
        postcode.setPersons(new HashSet<>(Collections.singletonList(person)));
        person.setPostal(new HashSet<>(Collections.singletonList(postcode)));
    }

    @Test
    void testCreatePerson() {
        when(personRepository.save(person)).thenReturn(person);

        Person savedPerson = personService.createPerson(person);

        assertNotNull(savedPerson);
        assertEquals(person.getName(), savedPerson.getName());
        verify(personRepository, times(1)).save(person);
    }

    @Test
    void testGetPersonById_CacheHit() {
        Long personId = 1L;
        String cacheKey = "person-" + personId;

        when(cache.getFromCache(cacheKey)).thenReturn(person);

        Person retrievedPerson = personService.getPersonById(personId);

        assertNotNull(retrievedPerson);
        assertEquals(person.getName(), retrievedPerson.getName());
        verify(cache, times(1)).getFromCache(cacheKey);
    }

    @Test
    void testGetPersonById_CacheMiss() {
        long personId = 1L;
        String cacheKey = "person-" + personId;

        when(cache.getFromCache(cacheKey)).thenReturn(null);
        when(personRepository.findById(Math.toIntExact(personId))).thenReturn(Optional.of(person));
        doNothing().when(cache).addToCache(cacheKey, person);

        Person retrievedPerson = personService.getPersonById(personId);

        assertNotNull(retrievedPerson);
        assertEquals(person.getName(), retrievedPerson.getName());
        verify(cache, times(1)).getFromCache(cacheKey);
        verify(personRepository, times(1)).findById(Math.toIntExact(personId));
        verify(cache, times(1)).addToCache(cacheKey, person);
    }

    @Test
    void testGetPersonById_EntityNotFoundException() {
        long personId = 1L;
        String cacheKey = "person-" + personId;

        when(cache.getFromCache(cacheKey)).thenReturn(null);
        when(personRepository.findById(Math.toIntExact(personId))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> personService.getPersonById(personId));
        verify(cache, times(1)).getFromCache(cacheKey);
        verify(personRepository, times(1)).findById(Math.toIntExact(personId));
    }

    @Test
    void testGetAllPeople() {
        List<Person> people = new ArrayList<>();
        people.add(person);

        when(personRepository.findAll()).thenReturn(people);

        List<Person> retrievedPeople = personService.getAllPeople();

        assertNotNull(retrievedPeople);
        assertEquals(1, retrievedPeople.size());
        assertEquals(person.getName(), retrievedPeople.get(0).getName());
        verify(personRepository, times(1)).findAll();
    }

    @Test
    void testGetAllPeopleWithPostCode() {
        List<Person> people = new ArrayList<>();
        people.add(person);

        when(personRepository.findAllPeopleWithPostCode(anyString())).thenReturn(people);

        List<Person> retrievedPeople = personService.getAllPeopleWithPostCode("m320jg"); // Передаем конкретное значение почтового кода

        assertNotNull(retrievedPeople);
        assertEquals(1, retrievedPeople.size());
        assertEquals(person.getName(), retrievedPeople.get(0).getName());
        verify(personRepository, times(1)).findAllPeopleWithPostCode(anyString()); // Проверяем, что метод был вызван с любой строкой
    }

    @Test
    void testUpdatePerson() {
        Long personId = 1L;
        String cacheKey = "person-" + personId;

        Person updatedPerson = new Person();
        updatedPerson.setName("Jane Doe");
        updatedPerson.setPostal(new HashSet<>(Collections.singletonList(postcode)));

        when(personRepository.findById(anyInt())).thenReturn(Optional.of(person));
        doNothing().when(cache).removeFromCache(cacheKey);
        when(personRepository.save(person)).thenReturn(updatedPerson);
        doNothing().when(cache).addToCache(argThat(argument -> argument.equals(cacheKey)), any(Person.class));

        Person returnedPerson = personService.updatePerson(personId, updatedPerson);

        assertNotNull(returnedPerson);
        assertEquals("Jane Doe", returnedPerson.getName());
        verify(personRepository, times(1)).findById(anyInt());
        verify(cache, times(1)).removeFromCache(cacheKey);
        verify(personRepository, times(1)).save(person);
    }

    @Test
    void testDeletePerson() {
        long personId = 1L;
        String cacheKey = "person-" + personId;

        when(personRepository.findById(Math.toIntExact(personId))).thenReturn(Optional.of(person));
        doNothing().when(cache).removeFromCache(cacheKey);
        doNothing().when(personRepository).deleteById(Math.toIntExact(personId));

        personService.deletePerson(personId);

        verify(personRepository, times(1)).findById(Math.toIntExact(personId));
        verify(cache, times(1)).removeFromCache(cacheKey);
        verify(personRepository, times(1)).deleteById(Math.toIntExact(personId));
    }
}