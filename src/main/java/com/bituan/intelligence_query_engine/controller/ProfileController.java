package com.bituan.intelligence_query_engine.controller;

import com.bituan.intelligence_query_engine.exception.BadRequest;
import com.bituan.intelligence_query_engine.exception.UnprocessableEntity;
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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // --------- APPLY FILTERS ----------------- //
        ProfileSpecs profileSpecs = new ProfileSpecs();
        Specification<Profile> spec = Specification
                .where(profileSpecs.isGender(queryParams.getGender()))
                .and(profileSpecs.isAgeGroup(queryParams.getAge_group()))
                .and(profileSpecs.isCountryId(queryParams.getCountry_id()))
                .and(profileSpecs.ageGreaterThanOrEqualTo(queryParams.getMin_age()))
                .and(profileSpecs.ageLessThanOrEqualTo(queryParams.getMax_age()))
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

    @GetMapping("/profiles/search")
    public ResponseEntity<ProfilesResponse> getProfilesByNaturalLanguage (@RequestParam("q") String query, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) {
        if (query.isBlank()) {
            throw new BadRequest("Missing or empty parameter");
        }

        if (limit != null && limit > 50) {
            throw new BadRequest("Limit cannot be greater than 50");
        }

        boolean canParse = false;

        ProfileSpecs profileSpecs = new ProfileSpecs();
        Specification<Profile> spec = Specification.where((root, q, builder) -> builder.conjunction());

        // Gender pattern
        Pattern pattern = Pattern.compile("\\b(male|female|men|women)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        Set<String> foundGenders = new HashSet<>();
        String gender = "";

        while (matcher.find()) {
            gender = matcher.group().equals("men") ? "male"
                    : matcher.group().equals("women") ? "female"
                    : matcher.group();
            foundGenders.add(gender);
        }

        if (!gender.isBlank() && !(foundGenders.contains("male") && foundGenders.contains("female"))) {
            spec = spec.and(profileSpecs.isGender(gender));
            canParse = true;
        }

        // 'young' group
        if (query.contains("young")) {
            spec = spec
                    .and(profileSpecs.ageGreaterThanOrEqualTo(16))
                    .and(profileSpecs.ageLessThanOrEqualTo(24));

            canParse = true;
        }

        // age group pattern
        pattern = Pattern.compile("\\b(child|teenager|adult|senior)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(query);

        while (matcher.find()) {
            spec = spec.and(profileSpecs.isAgeGroup(matcher.group().toLowerCase()));
            canParse = true;
        }

        // above age (age excluded) or from age (age included)
        pattern = Pattern.compile("\\b(above|from|older\\s+than|over)\\s+(\\d+)\\b", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(query);

        while (matcher.find()) {
            String textBeforeNumber = matcher.group(1);

            if (textBeforeNumber.equals("from")) {
                spec = spec.and(profileSpecs.ageGreaterThanOrEqualTo(Integer.valueOf(matcher.group(2))));
            } else {
                spec = spec.and(profileSpecs.ageGreaterThan(Integer.valueOf(matcher.group(2))));
            }

            canParse = true;
        }

        // below age
        pattern = Pattern.compile("\\b(below|to|younger\\s+than|under)\\s+(\\d+)\\b", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(query);

        while (matcher.find()) {
            String textBeforeNumber = matcher.group(1);

            if (textBeforeNumber.equals("to")) {
                spec = spec.and(profileSpecs.ageLessThanOrEqualTo(Integer.valueOf(matcher.group(2))));
            } else {
                spec = spec.and(profileSpecs.ageLessThan(Integer.valueOf(matcher.group(2))));
            }

            canParse = true;
        }

        // country
        for (String iso : Locale.getISOCountries()) {
            Locale locale = new Locale("", iso);
            String country = locale.getDisplayCountry();

            if (query.toLowerCase().contains(country.toLowerCase())) {
                spec = spec.and(profileSpecs.isCountryId(locale.getCountry()));
                canParse = true;
            }
        }

        if (!canParse) {
            throw new UnprocessableEntity("Unable to interpret query");
        }

        // pagination
        page = page == null ? 0 : page;
        limit = limit == null ? 10 : limit;
        Pageable pageable = PageRequest.of(page, limit);

        Page<Profile> profilePage = profileRepository.findAll(spec, pageable);

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
