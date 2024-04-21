package com.postcode.service;

import com.postcode.component.Cache;
import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.City;
import com.postcode.model.ZipCodeData;
import com.postcode.model.Person;
import com.postcode.model.PostCodeData;
import com.postcode.repository.CityRepository;
import com.postcode.repository.PostCodeRepository;
import com.postcode.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class PostCodeService {
    private static final String POSTCODE_API_URL = "https://api.postcodes.io/postcodes/%s";
    private static final Logger LOG = LoggerFactory.getLogger(PostCodeService.class);
    private final RestTemplate restTemplate;
    private final PostCodeRepository postRepository;
    private final PersonRepository personRepository;
    private final CityRepository cityRepository;
    private final Cache cache;
    private static final String ERROR_MESSAGE = "PostCode does not exist with given id: ";
    private static final String CACHE_KEY = "postcode-";
    private static final String CACHE_HIT = "Cash HAVE using key: %s";
    private static final String CACHE_MISS = "Cash EMPTY using key: %s";

    private final List<String> allowedPostalCodes = Arrays.asList("m320jg", "ne301dp", "ox495nu");

    public boolean isValidPostCode(String postCode) {
        return allowedPostalCodes.contains(postCode.toLowerCase());
    }

    public PostCodeService(RestTemplate restTemplate, PostCodeRepository postRepository, PersonRepository personRepository, CityRepository cityRepository, Cache cache) {
        this.restTemplate = restTemplate;
        this.postRepository = postRepository;
        this.personRepository = personRepository;
        this.cityRepository = cityRepository;
        this.cache = cache;
    }

    public ZipCodeData createPostCode(String post){
        String apiUrl = String.format(POSTCODE_API_URL, post);
        PostCodeData postCodeData = restTemplate.getForObject(apiUrl, PostCodeData.class);
        assert postCodeData != null;
        ZipCodeData zipCodeData = postCodeData.getResult();
        return postRepository.save(zipCodeData);
    }

    public List<ZipCodeData> addList(List<String> postalCode) {
        return postalCode.stream()
                .filter(this::isValidPostCode)
                .map(postCode -> {
                    int index = allowedPostalCodes.indexOf(postCode);
                    String post = allowedPostalCodes.get(index);
                    return createPostCode(post);
                })
                .toList();
    }

    public ZipCodeData getPostCodeDataById(Long postId) {
        return postRepository.findById(Math.toIntExact(postId)).orElseThrow(() -> new EntityNotFoundException(ERROR_MESSAGE + postId));
    }

    public ZipCodeData getPostCodeDataByName(String name) {
        String cacheKey = CACHE_KEY + name;
        ZipCodeData cachedPost = (ZipCodeData) cache.getFromCache(cacheKey);
        if (cachedPost != null){
            String logstash = String.format(CACHE_HIT,cacheKey);
            LOG.info(logstash);
            return cachedPost;
        }
        String logstash = String.format(CACHE_MISS, cacheKey);
        LOG.info(logstash);
        ZipCodeData zipCodeData = postRepository.findByPostcode(name);
        cache.addToCache(cacheKey, zipCodeData);
        return zipCodeData;
    }

    public List<ZipCodeData> getAllPostCodeData() {
        return postRepository.findAll();
    }

    public ZipCodeData updatePostCodeData(Long postId, ZipCodeData updatedZipCodeData) {
        ZipCodeData zipCodeData = postRepository.findById(Math.toIntExact(postId)).orElseThrow(() -> new EntityNotFoundException(ERROR_MESSAGE + postId));
        String cacheKey = CACHE_KEY + zipCodeData.getPostcode();
        cache.removeFromCache(cacheKey);

        zipCodeData.setPostcode(updatedZipCodeData.getPostcode());
        zipCodeData.setCity(updatedZipCodeData.getCity());
        zipCodeData.setPersons(updatedZipCodeData.getPersons());

        cache.addToCache(cacheKey, zipCodeData);
        return postRepository.save(zipCodeData);
    }

    public void deletePostCode(Long postId) {
        ZipCodeData zipCodeData = postRepository.findById(Math.toIntExact(postId)).orElseThrow(() -> new EntityNotFoundException(ERROR_MESSAGE + postId));
        if (zipCodeData.getPersons().size() != 0) {
            throw new EntityNotFoundException("Can't delete postcode " + postId + " because people are using it. Try deleting this post from a specified person.");
        }
        if (zipCodeData.getCity() != null) {
            throw new EntityNotFoundException("Can't delete postcode " + postId + " because it is in a city. Try deleting this post from a specified city.");
        }
        else if (zipCodeData != null) {
            String cacheKey = CACHE_KEY + zipCodeData.getPostcode();
            cache.removeFromCache(cacheKey);
            postRepository.deleteById(Math.toIntExact(postId));
        }
    }

    public void deletePostCodeFromPerson(Long postId, Long personId) {
        ZipCodeData zipCodeData = postRepository.findById(Math.toIntExact(postId)).orElseThrow(() -> new EntityNotFoundException(ERROR_MESSAGE + postId));
        Person person = personRepository.findById(Math.toIntExact(personId)).orElseThrow(() -> new EntityNotFoundException("Person does not exist with given id: " + personId));

        person.getPostal().remove(zipCodeData);
        zipCodeData.getPersons().remove(person);
        personRepository.save(person);
        postRepository.save(zipCodeData);
    }

    public void deletePostCodeFromCity(Long postId, Long cityId) {
        ZipCodeData zipCodeData = postRepository.findById(Math.toIntExact(postId)).orElseThrow(() -> new EntityNotFoundException(ERROR_MESSAGE + postId));
        City city = cityRepository.findById(Math.toIntExact(cityId)).orElseThrow(() -> new EntityNotFoundException("City does not exist with given id: " + cityId));

        city.getPostal().remove(zipCodeData);
        zipCodeData.setCity(null);
        cityRepository.save(city);
    }
}