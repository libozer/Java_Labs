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

    private final PostCodeService postCodeServiceService;
    private final List<String> allowedPostalCodes = Arrays.asList("m320jg", "ne301dp");

    private boolean isValidPostCode(String postCode) {
        return allowedPostalCodes.contains(postCode.toLowerCase());
    }
    @GetMapping
    public ResponseEntity<List<ZipCodeData>> getAllPostCodeData(){
        List<ZipCodeData> data = postCodeServiceService.getAllPostCodeData();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{name}")
    public ZipCodeData getPostCodeDataById(@PathVariable("name") String name){
        return postCodeServiceService.getPostCodeDataByName(name);
    }

    @PostMapping
    public ResponseEntity<ZipCodeData> create(@RequestBody String postalCode){
        if (!isValidPostCode(postalCode)) {
            throw new IllegalArgumentException("Invalid postcode: " + postalCode);
        }
        int index = allowedPostalCodes.indexOf(postalCode);
        String post = allowedPostalCodes.get(index);
        ZipCodeData zipCodeData = postCodeServiceService.createPostCode(post);
        return new ResponseEntity<>(zipCodeData, HttpStatus.CREATED);
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<String> deletePostCode(@PathVariable("post_id") Long postId) {
        postCodeServiceService.deletePostCode(postId);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @DeleteMapping("/{post_id}/person/{person_id}")
    public ResponseEntity<String> deletePostCodeFromPerson(@PathVariable("post_id") Long postId, @PathVariable("person_id") Long personId) {
        postCodeServiceService.deletePostCodeFromPerson(postId, personId);
        return ResponseEntity.ok("Post " + postId + " deleted successfully form person " + personId);
    }

    @DeleteMapping("/{post_id}/city/{city_id}")
    public ResponseEntity<String> deletePostCodeFromCity(@PathVariable("post_id") Long postId, @PathVariable("city_id") Long cityId) {
        postCodeServiceService.deletePostCodeFromCity(postId, cityId);
        return ResponseEntity.ok("Post " + postId + " deleted successfully form city " + cityId);
    }
}