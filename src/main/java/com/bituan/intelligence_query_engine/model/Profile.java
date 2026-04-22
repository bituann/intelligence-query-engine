package com.bituan.intelligence_query_engine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
public class Profile {
    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = org.hibernate.id.uuid.UuidVersion7Strategy.class)
    private UUID id;

    @Column(unique = true)
    private String name;

    private String gender;
    private double genderProbability;
    private int age;
    private String ageGroup;

    @Column(length = 2)
    private String countryId;

    private String countryName;
    private double countryProbability;
    private ZonedDateTime createdAt;
}
