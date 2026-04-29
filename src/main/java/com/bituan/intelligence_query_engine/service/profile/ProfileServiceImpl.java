package com.bituan.intelligence_query_engine.service.profile;

import com.bituan.intelligence_query_engine.enums.AgeGroup;
import com.bituan.intelligence_query_engine.exception.BadRequest;
import com.bituan.intelligence_query_engine.exception.NotFound;
import com.bituan.intelligence_query_engine.exception.UnprocessableEntity;
import com.bituan.intelligence_query_engine.model.entity.Profile;
import com.bituan.intelligence_query_engine.model.ProfileFilters;
import com.bituan.intelligence_query_engine.model.ProfilesPagination;
import com.bituan.intelligence_query_engine.model.response.ProfilesResponse;
import com.bituan.intelligence_query_engine.model.request.GetProfilesRequestQueryModel;
import com.bituan.intelligence_query_engine.model.response.AgifyApiResponse;
import com.bituan.intelligence_query_engine.model.response.GenderizeApiResponse;
import com.bituan.intelligence_query_engine.model.response.NationalizeApiResponse;
import com.bituan.intelligence_query_engine.model.response.ProfileResponse;
import com.bituan.intelligence_query_engine.repository.ProfileRepository;
import com.bituan.intelligence_query_engine.repository.specification.ProfileSpecs;
import com.bituan.intelligence_query_engine.service.external.ExternalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ExternalApiService externalApiService;

    private final ProfileRepository profileRepository;

    @Override
    public ProfilesResponse getProfiles(GetProfilesRequestQueryModel queryParams) {
        Pageable pageable = convertFiltersToPageRequest(queryParams.getPagination());
        Specification<Profile> spec = convertFiltersToDbQuery(queryParams.getFilters());

        Page<Profile> profilePage = profileRepository.findAll(spec, pageable);

        return buildProfilesResponse(profilePage);
    }

    @Override
    public ProfilesResponse getProfilesByNaturalLanguage(String query, Integer page, Integer limit) {
        if (query.isBlank()) {
            throw new BadRequest("Missing or empty parameter");
        }

        if (limit != null && limit > 50) {
            throw new BadRequest("Limit cannot be greater than 50");
        }

        if ((limit != null && limit < 1) || (page != null && page < 1)) {
            throw new BadRequest("Invalid limit or page");
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

            pattern = Pattern.compile("\\b" + Pattern.quote(country) + "\\b", Pattern.CASE_INSENSITIVE);

            if (pattern.matcher(query).find()) {
                spec = spec.and(profileSpecs.isCountryId(locale.getCountry()));
                canParse = true;
                break;
            }
        }

        if (!canParse) {
            throw new UnprocessableEntity("Unable to interpret query");
        }

        // pagination
        page = page == null ? 0 : page - 1;
        limit = limit == null ? 10 : limit;
        Pageable pageable = PageRequest.of(page, limit);

        Page<Profile> profilePage = profileRepository.findAll(spec, pageable);

        return buildProfilesResponse(profilePage);
    }

    @Override
    public ProfileResponse getProfile(String id) {
        Profile profile = profileRepository.findById(UUID.fromString(id)).orElseThrow(() -> new NotFound("Profile doesn't exist"));

        return ProfileResponse.builder()
                .status("success")
                .data(profile)
                .build();
    }

    @Override
    public ProfileResponse addProfile(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequest("Name is required");
        }

        if (profileRepository.existsByName(name)) {
            Profile profile = profileRepository
                    .findByName(name)
                    .orElseThrow(() -> new NotFound("Profile not found"));

            return ProfileResponse.builder()
                    .status("success")
                    .message("Profile already exists")
                    .data(profile)
                    .build();
        }

        GenderizeApiResponse genderizeApiResponse = externalApiService.genderize(name);
        AgifyApiResponse agifyApiResponse = externalApiService.agify(name);
        NationalizeApiResponse nationalizeApiResponse = externalApiService.nationalize(name);

        Profile profile = Profile.builder()
                .name(name)
                .gender(genderizeApiResponse.getGender())
                .genderProbability(genderizeApiResponse.getProbability())
                .age(agifyApiResponse.getAge())
                .ageGroup(AgeGroup.resolve(agifyApiResponse.getAge()))
                .countryId(nationalizeApiResponse.getHighestProbabilityCountryId())
                .countryName(new Locale.Builder()
                        .setRegion(nationalizeApiResponse.getHighestProbabilityCountryId())
                        .build()
                        .getDisplayCountry()
                )
                .countryProbability(nationalizeApiResponse.getHighestProbability())
                .build();

        profile = profileRepository.save(profile);

        return ProfileResponse.builder()
                .status("success")
                .data(profile)
                .build();
    }

    private Specification<Profile> convertFiltersToDbQuery (ProfileFilters filters) {
        if (filters == null) {
            return Specification.where((root, query, cb) -> cb.conjunction());
        }

        ProfileSpecs profileSpecs = new ProfileSpecs();

        return Specification
                .where(profileSpecs.isGender(filters.getGender()))
                .and(profileSpecs.isAgeGroup(filters.getAge_group() == null
                        ? null
                        : filters.getAge_group().name())
                )
                .and(profileSpecs.isCountryId(filters.getCountry_id()))
                .and(profileSpecs.ageGreaterThanOrEqualTo(filters.getMin_age()))
                .and(profileSpecs.ageLessThanOrEqualTo(filters.getMax_age()))
                .and(profileSpecs.countryProbabilityGreaterThan(filters.getMin_country_probability()))
                .and(profileSpecs.genderProbabilityGreaterThan(filters.getMin_gender_probability()));
    }

    private PageRequest convertFiltersToPageRequest (ProfilesPagination pagination) {
        if (pagination == null) {
            return PageRequest.of(0, 10);
        }

        if (pagination.getLimit() != null && pagination.getLimit() > 50) {
            throw new BadRequest("Limit cannot be greater than 50");
        }

        if (
                (pagination.getLimit() != null && pagination.getLimit() < 1) ||
                (pagination.getPage() != null && pagination.getPage() < 1)
        ) {
            throw new BadRequest("Invalid limit or page");
        }

        Sort.Direction direction = Sort.Direction.fromOptionalString(pagination.getOrder()).orElse(Sort.Direction.ASC);
        Sort sort = pagination.getSort_by() == null || pagination.getSort_by().isBlank() ? Sort.unsorted() : Sort.by(direction, toCamelCase(pagination.getSort_by()));

        int page = pagination.getPage() == null ? 0 : pagination.getPage() - 1;
        int limit = pagination.getLimit() == null ? 10 : pagination.getLimit();

        return PageRequest.of(page, limit, sort);
    }

    private String generatePaginationLink (int page, int limit) {
        if (page < 0) return null;

        return "/api/profiles?page=%d&limit=%d".formatted(page + 1, limit);
    }

    private ProfilesResponse buildProfilesResponse (Page<Profile> profilesPage) {
        int page = profilesPage.getNumber();
        int limit = profilesPage.getSize();

        return ProfilesResponse.builder()
                .status("success")
                .page(page + 1)
                .limit(limit)
                .total((int) profilesPage.getTotalElements())
                .totalPages(profilesPage.getTotalPages())
                .links(ProfilesResponse.Links.builder()
                        .self(generatePaginationLink(page, limit))
                        .next(profilesPage.hasNext() ? generatePaginationLink(page + 1, limit) : null)
                        .prev(generatePaginationLink(page - 1, limit))
                        .build()
                )
                .data(profilesPage.toList())
                .build();
    }

    private String toCamelCase(String snakeCase) {
        if (snakeCase == null || !snakeCase.contains("_")) {
            return snakeCase;
        }
        StringBuilder result = new StringBuilder();
        String[] parts = snakeCase.split("_");
        for (int i = 0; i < parts.length; i++) {
            String s = parts[i];
            if (i == 0) {
                result.append(s.toLowerCase());
            } else {
                result.append(Character.toUpperCase(s.charAt(0)))
                        .append(s.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

}
