package com.bituan.intelligence_query_engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QueryModel {
    private String gender;
    private String age_group;
    private String country_id;
    private Integer min_age;
    private Integer max_age;
    private Double min_gender_probability;
    private Double min_country_probability;
    private String sort_By;
    private String order;
    private Integer page;
    private Integer limit;
}
