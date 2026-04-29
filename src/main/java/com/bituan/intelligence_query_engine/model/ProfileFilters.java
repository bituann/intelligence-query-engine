package com.bituan.intelligence_query_engine.model;

import com.bituan.intelligence_query_engine.enums.AgeGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileFilters {
    private String gender;
    private AgeGroup age_group;
    private String country_id;
    private Integer min_age;
    private Integer max_age;
    private Double min_gender_probability;
    private Double min_country_probability;
}
