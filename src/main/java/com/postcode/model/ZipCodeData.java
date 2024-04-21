package com.postcode.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ZipCodeData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postId")
    private Long postId;

    private String postcode;
    private String eastings;
    private String northings;
    private String country;
    private String region;
    private String incode;
    private String outcode;
    private String nuts;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @JsonIgnore
    @ManyToMany(mappedBy = "postal")
    private Set<com.postcode.model.Person> persons;

    public ZipCodeData(Long id){
        this.postId = id;
    }

}