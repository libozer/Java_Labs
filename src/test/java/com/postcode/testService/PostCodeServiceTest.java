package com.postcode.testService;

import com.postcode.component.Cache;
import com.postcode.exception.EntityNotFoundException;
import com.postcode.model.City;
import com.postcode.model.ZipCodeData;
import com.postcode.model.PostCodeData;
import com.postcode.model.Person;
import com.postcode.repository.CityRepository;
import com.postcode.repository.PostCodeRepository;
import com.postcode.repository.PersonRepository;
import com.postcode.service.PostCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCodeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PostCodeRepository postCodeRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private Cache cache;

    @Mock
    private List<String> allowedPostalCodes;

    @InjectMocks
    private PostCodeService postCodeService;

    private ZipCodeData zipCodeData;
    private PostCodeData postCodeData;
    private Person person;
    private City city;

    @BeforeEach
    void setUp() {
        zipCodeData = new ZipCodeData(1L);
        zipCodeData.setPostcode("m320jg");
        zipCodeData.setPersons(new HashSet<>());
        zipCodeData.setCity(null);

        postCodeData = new PostCodeData();
        postCodeData.setResult(zipCodeData);

        person = new Person();
        person.setId(1L);
        person.setPostal(new HashSet<>(Collections.singletonList(zipCodeData)));

        city = new City();
        city.setId(1L);
        city.setPostal(new HashSet<>(Collections.singletonList(zipCodeData)));
    }

    @Test
    void testCreatePostCode() {
        when(restTemplate.getForObject(anyString(), any(Class.class))).thenReturn(postCodeData);
        when(postCodeRepository.save(any(ZipCodeData.class))).thenReturn(zipCodeData);

        ZipCodeData createdCryptoData = postCodeService.createPostCode("M32 0JG");

        assertEquals(zipCodeData, createdCryptoData);
        verify(restTemplate, times(1)).getForObject(anyString(), any(Class.class));
        verify(postCodeRepository, times(1)).save(any(ZipCodeData.class));
    }

    @Test
    void testGetPostCodeDataById() {
        when(postCodeRepository.findById(anyInt()))
                .thenReturn(Optional.of(zipCodeData));

        ZipCodeData retrievedCryptoData = postCodeService.getPostCodeDataById(1L);

        assertEquals(zipCodeData, retrievedCryptoData);
        verify(postCodeRepository, times(1)).findById(anyInt());
    }

    @Test
    void testGetPostCodeDataByName() {
        when(cache.getFromCache(anyString()))
                .thenReturn(null);
        when(postCodeRepository.findByPostcode(anyString()))
                .thenReturn(zipCodeData);
        doNothing().when(cache).addToCache(anyString(), any(ZipCodeData.class));

        ZipCodeData retrievedCryptoData = postCodeService.getPostCodeDataByName("M32 0JG");

        assertEquals(zipCodeData, retrievedCryptoData);
        verify(cache, times(1)).getFromCache(anyString());
        verify(postCodeRepository, times(1)).findByPostcode(anyString());
        verify(cache, times(1)).addToCache(anyString(), any(ZipCodeData.class));
    }

    @Test
    void testGetAllPostCode() {
        when(postCodeRepository.findAll())
                .thenReturn(Collections.singletonList(zipCodeData));

        List<ZipCodeData> allPostCodeData = postCodeService.getAllPostCodeData();

        assertEquals(1, allPostCodeData.size());
        assertEquals(zipCodeData, allPostCodeData.get(0));
        verify(postCodeRepository, times(1)).findAll();
    }

    @Test
    void testUpdatePostCodeData() {
        when(postCodeRepository.findById(anyInt()))
                .thenReturn(Optional.of(zipCodeData));
        doNothing().when(cache).removeFromCache(anyString());
        when(postCodeRepository.save(any(ZipCodeData.class)))
                .thenReturn(zipCodeData);
        doNothing().when(cache).addToCache(anyString(), any(ZipCodeData.class));

        ZipCodeData updatedPostCodeData = postCodeService.updatePostCodeData(1L, zipCodeData);

        assertEquals(zipCodeData, updatedPostCodeData);
        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cache, times(1)).removeFromCache(anyString());
        verify(postCodeRepository, times(1)).save(any(ZipCodeData.class));
        verify(cache, times(1)).addToCache(anyString(), any(ZipCodeData.class));
    }

    @Test
    void testDeletePostCode() {
        when(postCodeRepository.findById(anyInt()))
                .thenReturn(Optional.of(zipCodeData));
        doNothing().when(cache).removeFromCache(anyString());
        doNothing().when(postCodeRepository).deleteById(anyInt());

        postCodeService.deletePostCode(1L);

        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cache, times(1)).removeFromCache(anyString());
        verify(postCodeRepository, times(1)).deleteById(anyInt());
    }

    @Test
    void testDeletePostCodeFromPerson() {
        when(postCodeRepository.findById(anyInt()))
                .thenReturn(Optional.of(zipCodeData));
        when(personRepository.findById(anyInt()))
                .thenReturn(Optional.of(person));
        when(personRepository.save(any(Person.class)))
                .thenReturn(person);
        when(postCodeRepository.save(any(ZipCodeData.class)))
                .thenReturn(zipCodeData);

        postCodeService.deletePostCodeFromPerson(1L, 1L);

        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(personRepository, times(1)).findById(anyInt());
    }

    @Test
    void testGetPostCodeDataByIdNotFound() {
        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> postCodeService.getPostCodeDataById(1L));
        verify(postCodeRepository, times(1)).findById(anyInt());
    }

    @Test
    void testUpdatePostCodeDataNotFound() {
        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> postCodeService.updatePostCodeData(3L, zipCodeData));
        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cache, never()).removeFromCache(anyString());
        verify(postCodeRepository, never()).save(any(ZipCodeData.class));
        verify(cache, never()).addToCache(anyString(), any(ZipCodeData.class));
    }

    @Test
    void testDeletePostCodeWithPersonsAndCity() {
        zipCodeData.setCity(new City());
        zipCodeData.setPersons(new HashSet<>(List.of(new Person())));

        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.of(zipCodeData));

        assertThrows(EntityNotFoundException.class, () -> postCodeService.deletePostCode(1L));

        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cache, never()).removeFromCache(anyString());
        verify(postCodeRepository, never()).deleteById(anyInt());
    }

    @Test
    void testDeletePostCodeWithPersons() {
        zipCodeData.setPersons(new HashSet<>(List.of(new Person())));

        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.of(zipCodeData));

        assertThrows(EntityNotFoundException.class, () -> postCodeService.deletePostCode(1L));

        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cache, never()).removeFromCache(anyString());
        verify(postCodeRepository, never()).deleteById(anyInt());
    }

    @Test
    void testDeletePostCodeWithCity() {
        zipCodeData.setCity(new City());

        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.of(zipCodeData));

        assertThrows(EntityNotFoundException.class, () -> postCodeService.deletePostCode(1L));

        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cache, never()).removeFromCache(anyString());
        verify(postCodeRepository, never()).deleteById(anyInt());
    }

    @Test
    void testDeletePostCodeFromPersonPersonNotFound() {
        when(personRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.of(zipCodeData));

        assertThrows(EntityNotFoundException.class, () -> postCodeService.deletePostCodeFromPerson(1L, 4L));

        verify(personRepository, times(1)).findById(anyInt());
        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(personRepository, never()).save(any(Person.class));
        verify(postCodeRepository, never()).save(any(ZipCodeData.class));
    }

    @Test
    void testDeletePostCodeFromPersonPostCodeNotFound() {
        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> postCodeService.deletePostCodeFromPerson(4L, 1L));

        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(personRepository, never()).findById(anyInt());
        verify(personRepository, never()).save(any(Person.class));
        verify(postCodeRepository, never()).save(any(ZipCodeData.class));
    }

    @Test
    void testDeletePostCodeNotFound() {
        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> postCodeService.deletePostCode(4L));

        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cache, never()).removeFromCache(anyString());
        verify(postCodeRepository, never()).deleteById(anyInt());
    }

    @Test
    void testDeletePostCodeFromCity() {
        City city = new City();
        city.setId(3L);
        city.setPostal(new HashSet<>(Collections.singletonList(zipCodeData)));

        when(cityRepository.findById(anyInt())).thenReturn(Optional.of(city));
        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.of(zipCodeData));
        when(cityRepository.save(any(City.class))).thenReturn(city);

        postCodeService.deletePostCodeFromCity(1L, city.getId());

        verify(cityRepository, times(1)).findById(anyInt());
        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cityRepository, times(1)).save(any(City.class));
        assertNull(zipCodeData.getCity());
        assertFalse(city.getPostal().contains(zipCodeData));
    }

    @Test
    void testDeletePostCodeFromCityCityNotFound() {
        when(cityRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(postCodeRepository.findById(anyInt())).thenReturn(Optional.of(zipCodeData));

        assertThrows(EntityNotFoundException.class, () -> postCodeService.deletePostCodeFromCity(1L, 4L));

        verify(cityRepository, times(1)).findById(anyInt());
        verify(postCodeRepository, times(1)).findById(anyInt());
        verify(cityRepository, never()).save(any(City.class));
    }

    @Test
    void testInvalidPostCode() {
        assertFalse(postCodeService.isValidPostCode("ABCDE")); // Проверяем невалидный почтовый индекс
    }
}