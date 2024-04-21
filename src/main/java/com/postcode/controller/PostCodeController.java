package com.postcode.controller;

import com.postcode.model.ZipCodeData;
import com.postcode.service.PostCodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/postcode")
public class PostCodeController {

    private final PostCodeService postCodeService;
    private final List<String> allowedPostalCodes = Arrays.asList("m320jg", "ne301dp", "ox495nu");

    private boolean isValidPostCode(String postCode) {
        return allowedPostalCodes.contains(postCode.toLowerCase());
    }

    @GetMapping
    public ResponseEntity<List<ZipCodeData>> getAllPostCodeData(){
        List<ZipCodeData> data = postCodeService.getAllPostCodeData();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{name}")
    public ZipCodeData getPostCodeDataById(@PathVariable("name") String name){
        return postCodeService.getPostCodeDataByName(name);
    }

    @PostMapping
    public ResponseEntity<ZipCodeData> create(@RequestBody String postalCode){
        if (!isValidPostCode(postalCode)) {
            throw new IllegalArgumentException("Invalid postcode: " + postalCode);
        }
        int index = allowedPostalCodes.indexOf(postalCode);
        String post = allowedPostalCodes.get(index);
        ZipCodeData zipCodeData = postCodeService.createPostCode(post);
        return new ResponseEntity<>(zipCodeData, HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ZipCodeData>> bulkOperation(@RequestBody List<String> cryptoCurrencies) {
        List<ZipCodeData> result = postCodeService.addList(cryptoCurrencies);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<String> deletePostCode(@PathVariable("post_id") Long postId) {
        postCodeService.deletePostCode(postId);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @DeleteMapping("/{post_id}/person/{person_id}")
    public ResponseEntity<String> deletePostCodeFromPerson(@PathVariable("post_id") Long postId, @PathVariable("person_id") Long personId) {
        postCodeService.deletePostCodeFromPerson(postId, personId);
        return ResponseEntity.ok("Post " + postId + " deleted successfully form person " + personId);
    }

    @DeleteMapping("/{post_id}/city/{city_id}")
    public ResponseEntity<String> deletePostCodeFromCity(@PathVariable("post_id") Long postId, @PathVariable("city_id") Long cityId) {
        postCodeService.deletePostCodeFromCity(postId, cityId);
        return ResponseEntity.ok("Post " + postId + " deleted successfully form city " + cityId);
    }
}