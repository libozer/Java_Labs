package com.postcode.service;
import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.City;
import com.postcode.model.ZipCodeData;
import com.postcode.model.Person;
import com.postcode.model.PostCodeData;
import com.postcode.repository.CityRepository;
import com.postcode.repository.PostCodeRepository;
import com.postcode.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PostCodeService {
    private static final String POSTCODE_API_URL = "https://api.postcodes.io/postcodes/%s";

    private final RestTemplate restTemplate;
    private final PostCodeRepository cryptoRepository;
    private final PersonRepository personRepository;
    private final CityRepository cityRepository;
    private static final String ERROR_MESSAGE = "PostCode does not exist with given id: ";

    public PostCodeService(RestTemplate restTemplate, PostCodeRepository cryptoRepository, PersonRepository personRepository, CityRepository cityRepository) {
        this.restTemplate = restTemplate;
        this.cryptoRepository = cryptoRepository;
        this.personRepository = personRepository;
        this.cityRepository = cityRepository;
    }

    public ZipCodeData createPostCode(String post){
        String apiUrl = String.format(POSTCODE_API_URL, post);
        PostCodeData postCodeData = restTemplate.getForObject(apiUrl, PostCodeData.class);
        assert postCodeData != null;
        ZipCodeData zipCodeData = postCodeData.getResult();
        return cryptoRepository.save(zipCodeData);
    }

    public ZipCodeData getPostCodeDataById(Long postId) {
        return cryptoRepository.findById(Math.toIntExact(postId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + postId)
        );
    }

    public ZipCodeData getPostCodeDataByName(String name) {
        return cryptoRepository.findByPostcode(name);
    }

    public List<ZipCodeData> getAllPostCodeData() {
        return cryptoRepository.findAll();
    }

    public ZipCodeData updatePostCodeData(Long postId, ZipCodeData updatedZipCodeData) {
        ZipCodeData zipCodeData = cryptoRepository.findById(Math.toIntExact(postId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + postId)
        );

        zipCodeData.setPostcode(updatedZipCodeData.getPostcode());
        zipCodeData.setCity(updatedZipCodeData.getCity());
        zipCodeData.setPersons(updatedZipCodeData.getPersons());

        return cryptoRepository.save(zipCodeData);
    }

    public void deletePostCode(Long postId) {
        ZipCodeData zipCodeData = cryptoRepository.findById(Math.toIntExact(postId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + postId)
        );
        if (!zipCodeData.getPersons().isEmpty()){
            throw new EntityNotFoundException("Can't delete postcode " + postId + " because people are using it. Try deleting this post from a specified person.");
        }
        if (zipCodeData.getCity() != null){
            throw new EntityNotFoundException("Can't delete postcode " + postId + " because it is in a city. Try deleting this post from a specified city.");
        }
        cryptoRepository.deleteById(Math.toIntExact(postId));
    }

    public void deletePostCodeFromPerson(Long postId, Long personId) {
        ZipCodeData zipCodeData = cryptoRepository.findById(Math.toIntExact(postId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + postId)
        );
        Person person = personRepository.findById(Math.toIntExact(personId)).orElseThrow(
                () -> new EntityNotFoundException("Person does not exist with given id: " + personId)
        );

        person.getPostal().remove(zipCodeData);
        zipCodeData.getPersons().remove(person);
        personRepository.save(person);
        cryptoRepository.save(zipCodeData);
    }

    public void deletePostCodeFromCity(Long postId, Long cityId) {
        ZipCodeData zipCodeData = cryptoRepository.findById(Math.toIntExact(postId)).orElseThrow(
                () -> new EntityNotFoundException(ERROR_MESSAGE + postId)
        );
        City city = cityRepository.findById(Math.toIntExact(cityId)).orElseThrow(
                () -> new EntityNotFoundException("City does not exist with given id: " + cityId)
        );

        city.getPostal().remove(zipCodeData);
        zipCodeData.setCity(null);
        cityRepository.save(city);
    }
}