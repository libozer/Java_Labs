package com.postcode.service;

import com.postcode.component.Cache;
import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.ZipCodeData;
import com.postcode.model.Person;
import com.postcode.repository.PersonRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PersonService {
    private static final Logger LOG = LoggerFactory.getLogger(PersonService.class);

    private PersonRepository personRepository;
    private final Cache cache;
    private static final  String ERROR_MESSAGE = "Person does not exist with given id: ";
    private static final String CACHE_HIT = "Cash HAVE using key: %s";
    private static final String CACHE_MISS = "Cash EMPTY using key: %s";
    private static final String CACHE_KEY = "person-";

    public Person createPerson(Person person) {
        return personRepository.save(person);
    }

    public Person getPersonById(Long personId) {
        String cacheKey = CACHE_KEY + personId;
        Person cachedPerson = (Person) cache.getFromCache(cacheKey);
        if (cachedPerson != null){
            String logstash = String.format(CACHE_HIT, cacheKey);
            LOG.info(logstash);
            return cachedPerson;
        }
        String logstash = String.format(CACHE_MISS, cacheKey);
        LOG.info(logstash);
        Person personFromRepo = personRepository.findById(Math.toIntExact(personId))
                .orElseThrow(()->
                        new EntityNotFoundException(ERROR_MESSAGE + personId));
        cache.addToCache(cacheKey, personFromRepo);
        return personFromRepo;
    }

    public List<Person> getAllPeople() {
        return personRepository.findAll();
    }

    public List<Person> getAllPeopleWithPostCode(String cryptoName){
        return personRepository.findAllPeopleWithPostCode(cryptoName);
    }

    public Person updatePerson(Long personId, Person updatedPerson) {
        Person person = personRepository.findById(Math.toIntExact(personId)).orElseThrow(() -> new EntityNotFoundException(ERROR_MESSAGE + personId));
        String cacheKey = CACHE_KEY + person.getId();
        cache.removeFromCache(cacheKey);
        person.setName(updatedPerson.getName());
        person.setPostal(updatedPerson.getPostal());

        return personRepository.save(person);
    }

    @Transactional
    public void deletePerson(Long personId) {
        Person person = personRepository.findById(Math.toIntExact(personId)).orElseThrow(() -> new EntityNotFoundException("Person with ID " + personId + " not found"));

        for (ZipCodeData zipCodeData : person.getPostal()) {
            zipCodeData.getPersons().remove(person);
        }

        String cacheKey = CACHE_KEY + personId;
        cache.removeFromCache(cacheKey);
        person.getPostal().clear();
        personRepository.deleteById(Math.toIntExact(personId));
    }
}