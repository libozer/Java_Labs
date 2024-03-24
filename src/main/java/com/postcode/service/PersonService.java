package com.postcode.service;

import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.ZipCodeData;
import com.postcode.model.Person;
import com.postcode.repository.PostCodeRepository;
import com.postcode.repository.PersonRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class PersonService {

    private PersonRepository personRepository;
    private PostCodeRepository cryptoRepository;
    private static final  String ERROR_MESSAGE = "Person does not exist with given id: ";

    public Person createPerson(Person person) {
        return personRepository.save(person);
    }


    public Person getPersonById(Long personId) {
        return personRepository.findById(Math.toIntExact(personId))
                .orElseThrow(()->
                        new EntityNotFoundException(ERROR_MESSAGE + personId));
    }


    public List<Person> getAllPeople() {
        return personRepository.findAll();
    }


    public Person updatePerson(Long personId, Person updatedPerson) {
        Person person = personRepository.findById(Math.toIntExact(personId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + personId)
        );

        person.setName(updatedPerson.getName());
        person.setPostal(updatedPerson.getPostal());

        return personRepository.save(person);
    }


    @Transactional
    public void deletePerson(Long personId) {
        Person person = personRepository.findById(Math.toIntExact(personId)).orElseThrow(
                () -> new EntityNotFoundException("Person with ID " + personId + " not found")
        );

        // Remove the person from the cryptocurrencies they're associated with
        for (ZipCodeData zipCodeData : person.getPostal()) {
            zipCodeData.getPersons().remove(person);
        }

        // Update the changes in the database
        person.getPostal().clear();
        personRepository.deleteById(Math.toIntExact(personId));
    }
}