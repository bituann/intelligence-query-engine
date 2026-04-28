package com.bituan.intelligence_query_engine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Table(name = "profiles")
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;
}
