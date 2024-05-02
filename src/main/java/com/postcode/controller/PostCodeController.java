package com.postcode.controller;

import com.postcode.model.ZipCodeData;
import com.postcode.model.Person;
import com.postcode.model.City;
import com.postcode.service.PostCodeService;
import com.postcode.service.CounterService;
import com.postcode.service.PersonService;
import com.postcode.service.CityService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/api/v1/postcode")
public class PostCodeController {

    private static final String SUCCESSSTATUS = "Method success";
    private static final String ERROR_METHOD = "errorMethod";
    private static final String SUCCESS_METHOD = "successMethod";
    private final PostCodeService postCodeService;
    private final PersonService personService;
    private final CityService cityService;
    private final List<String> allowedPostalCodes = Arrays.asList("m320jg", "ne301dp", "ox495nu");

    private boolean isValidPostCode(String postCode) {
        return allowedPostalCodes.contains(postCode.toLowerCase());
    }

    private static final Logger LOG = LoggerFactory.getLogger(PostCodeController.class);
//    @GetMapping
//    public ResponseEntity<List<ZipCodeData>> getAllPostCodeData(){
//        CounterService.enhanceCounter();
//        List<ZipCodeData> data = postCodeService.getAllPostCodeData();
//        return ResponseEntity.ok(data);
//    }

    @GetMapping("/allInfo")
    public String getAllPostCodeData(Model model) {
        CounterService.enhanceCounter();
        List<ZipCodeData> post = postCodeService.getAllPostCodeData();
        model.addAttribute("postCode", post);
        List<Person> people = personService.getAllPeople();
        model.addAttribute("people", people);
        List<City> cities = cityService.getAllCities();
        model.addAttribute("cities", cities);
        return "allInfo";
    }

    @GetMapping("/{name}")
    public ZipCodeData getPostCodeDataById(@PathVariable("name") String name) {
        return postCodeService.getPostCodeDataByName(name);
    }

    @GetMapping("/create_postcodedata")
    public String showCreatePostCodeDataForm(Model model) {
        return "savePostCodeData";
    }

//    @PostMapping
//    public ResponseEntity<ZipCodeData> create(@RequestBody String postalCode){
//        if (!isValidPostCode(postalCode)) {
//            throw new IllegalArgumentException("Invalid postcode: " + postalCode);
//        }
//        int index = allowedPostalCodes.indexOf(postalCode);
//        String post = allowedPostalCodes.get(index);
//        ZipCodeData zipCodeData = postCodeService.createPostCode(post);
//        return new ResponseEntity<>(zipCodeData, HttpStatus.CREATED);
//    }

    @PostMapping("/create")
    public String create(@RequestParam(required = false, name = "postCode") String postalCode) {
        if (!isValidPostCode(postalCode)) {
            return ERROR_METHOD;
        }
        int index = allowedPostalCodes.indexOf(postalCode);
        String post = allowedPostalCodes.get(index);
        if (postCodeService.createPostCode(post)) {
            return SUCCESS_METHOD;
        }
        return ERROR_METHOD;
    }

    @PostMapping("/bulk")
    public void bulkOperation(@RequestBody List<String> postalCodes) {
        postCodeService.addList(postalCodes);
    }

    @DeleteMapping("/{post_id}")
    public void deletePostCode(@PathVariable("post_id") Long postId) {
        postCodeService.deletePostCode(postId);
    }

    @GetMapping("/deletePostCodeHTML")
    public String getDeletePostCodeHTML() {
        return "deletePostCode";
    }

    @PostMapping("/deletePostCodeHTML")
    public String deletePostCodeHTML(@RequestParam(required = false, name = "idPost") String post_id) {
        Long postId = Long.parseLong(post_id);
        if (postCodeService.deletePostCode(postId)) {
            return SUCCESS_METHOD;
        }
        return ERROR_METHOD;
    }

    @GetMapping("/deletePostCodeFromPersonHTML")
    public String deleteTest() {
        return "deletePostCodeFromPerson";
    }

    @GetMapping("/deletePostCodeFromCityHTML")
    public String deleteTest2() {
        return "deletePostCodeFromCity";
    }

    @DeleteMapping("/{post_id}/person/{person_id}")
    public ResponseEntity<String> deletePostCodeFromPerson(@PathVariable("post_id") Long postId, @PathVariable("person_id") Long personId) {
        postCodeService.deletePostCodeFromPerson(postId, personId);
        return ResponseEntity.ok("Post " + postId + " deleted successfully form person " + personId);
    }

    @PostMapping("/deletePostCodeFromPersonHTML")
    public String deletePostCodeFromPersonHTML(@RequestParam(required = false, name = "idPost") String post_id, @RequestParam(required = false, name = "idPerson") String person_id) {
        Long postId = Long.parseLong(post_id);
        Long personId = Long.parseLong(person_id);
        if (postCodeService.deletePostCodeFromPerson(postId, personId)) {
            return SUCCESS_METHOD;
        }
        return ERROR_METHOD;
    }

    @DeleteMapping("/{post_id}/city/{city_id}")
    public ResponseEntity<String> deletePostCodeFromCity(@PathVariable("post_id") Long postId, @PathVariable("city_id") Long cityId) {
        postCodeService.deletePostCodeFromCity(postId, cityId);
        return ResponseEntity.ok("Post " + postId + " deleted successfully form city " + cityId);
    }


    @PostMapping("/deletePostCodeFromCityHTML")
    public String deletePostCodeFromCityHTML(@RequestParam(required = false, name = "idPost") String post_id, @RequestParam(required = false, name = "idCity") String city_id) {
        Long postId = Long.parseLong(post_id);
        Long cityId = Long.parseLong(city_id);
        if (postCodeService.deletePostCodeFromCity(postId, cityId)) {
            return SUCCESS_METHOD;
        }
        return ERROR_METHOD;
    }

    @GetMapping("/addPostCodeToPersonHTML")
    public String getAddPostCodeToPersonHTML(){
        return "addPostCodeToPerson";
    }
    @GetMapping("/addPostCodeToCityHTML")
    public String getAddPostCodeToCityHTML(){
        return "addPostCodeToCity";
    }

    @PostMapping("/addPostCodeToPersonHTML")
    public String AddPostCodeToPersonHTML(@RequestParam(required = false, name = "idPost") String post_id, @RequestParam(required = false, name = "idPerson") String person_id) {
        Long postId = Long.parseLong(post_id);
        Long personId = Long.parseLong(person_id);
        Person person = personService.getPersonById(personId);
        ZipCodeData postCodeData = postCodeService.getPostCodeDataById(postId);
        if (person == null || postCodeData == null) {
            return ERROR_METHOD;
        }
        person.addPostCode(postCodeData);
        personService.updatePerson(personId, person);

        return SUCCESS_METHOD;
    }

    @PostMapping("/addPostCodeToCityHTML")
    public String AddCryptoToChainHTML(@RequestParam(required = false, name = "idPost") String post_id, @RequestParam(required = false, name = "idCity") String city_id) {
        Long postId = Long.parseLong(post_id);
        Long cityId = Long.parseLong(city_id);
        City city = cityService.getCityById(cityId);
        ZipCodeData postCodeData = postCodeService.getPostCodeDataById(postId);
        if (city == null || postCodeData == null){
            return ERROR_METHOD;
        }
        city.addPostCode(postCodeData);
        postCodeData.setCity(city);
        postCodeService.updatePostCodeData(postId, postCodeData);
        cityService.updateCity(cityId, city);
        return SUCCESS_METHOD;
    }
}
