package com.bituan.intelligence_query_engine.controller;

import com.bituan.intelligence_query_engine.exception.BadRequest;
import com.bituan.intelligence_query_engine.model.Profile;
import com.bituan.intelligence_query_engine.model.ProfilesResponse;
import com.bituan.intelligence_query_engine.model.QueryModel;
import com.bituan.intelligence_query_engine.repository.ProfileRepository;
import com.bituan.intelligence_query_engine.repository.specification.ProfileSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class ProfileController {
    private final ProfileRepository profileRepository;

    @GetMapping("/profiles")
    public ResponseEntity<ProfilesResponse> getProfiles (QueryModel queryParams) {
        if (queryParams.getLimit() != null && queryParams.getLimit() > 50) {
            throw new BadRequest("Limit cannot be greater than 50");
        }

        // --------- HANDLE PAGINATION ------------- //
        Sort.Direction direction = Sort.Direction.fromOptionalString(queryParams.getOrder()).orElse(Sort.Direction.ASC);
        Sort sort = queryParams.getSort_By() == null || queryParams.getSort_By().isBlank() ? Sort.unsorted() : Sort.by(direction, queryParams.getSort_By());

        int page = queryParams.getPage() == null ? 0 : queryParams.getPage();
        int limit = queryParams.getLimit() == null ? 10 : queryParams.getLimit();

        Pageable pageable = PageRequest.of(page, limit, sort);
        // --------- END :: HANDLE PAGINATION ------------- //

        System.out.println(queryParams.getMax_age());

        // --------- APPLY FILTERS ----------------- //
        ProfileSpecs profileSpecs = new ProfileSpecs();
        Specification<Profile> spec = Specification
                .where(profileSpecs.isGender(queryParams.getGender()))
                .and(profileSpecs.isAgeGroup(queryParams.getAge_group()))
                .and(profileSpecs.isCountryId(queryParams.getCountry_id()))
                .and(profileSpecs.ageGreaterThan(queryParams.getMin_age()))
                .and(profileSpecs.ageLessThan(queryParams.getMax_age()))
                .and(profileSpecs.countryProbabilityGreaterThan(queryParams.getMin_country_probability()))
                .and(profileSpecs.genderProbabilityGreaterThan(queryParams.getMin_gender_probability()));

        Page<Profile> profilePage = profileRepository.findAll(spec, pageable);
        // --------- END :: APPLY FILTERS ----------------- //

        ProfilesResponse response = ProfilesResponse.builder()
                .status("success")
                .page(profilePage.getNumber())
                .limit(profilePage.getSize())
                .total(profilePage.getNumberOfElements())
                .data(profilePage.toList())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
