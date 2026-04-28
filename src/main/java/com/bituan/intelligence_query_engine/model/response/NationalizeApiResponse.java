package com.bituan.intelligence_query_engine.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class NationalizeApiResponse {
    private int count;
    private String name;
    private List<CountryData> country;

    @Data
    private static class CountryData {
        @JsonProperty("country_id")
        private String countryId;
        private double probability;
    }

    public double getHighestProbability () {
        return getHighestProbabilityCountry().getProbability();
    }

    public String getHighestProbabilityCountryId () {
        return getHighestProbabilityCountry().getCountryId();
    }

    private CountryData getHighestProbabilityCountry () {
        return this.country
                .stream()
                .reduce((a, b) -> a.getProbability() > b.getProbability() ? a : b)
                .orElse(null);
    }
}
