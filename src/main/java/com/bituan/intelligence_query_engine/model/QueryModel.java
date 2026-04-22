package com.bituan.intelligence_query_engine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class QueryModel {
    private String gender;
    private String ageGroup;
    private String countryId;
    private Integer minAge;
    private Integer maxAge;
    private Double minGenderProbability;
    private Double minCountryProbability;
    private String sortBy;
    private String order;
    private Integer page;
    private Integer limit;
}
