package com.bituan.intelligence_query_engine.controller;

import com.bituan.intelligence_query_engine.exception.BadRequest;
import com.bituan.intelligence_query_engine.exception.ServerException;
import com.bituan.intelligence_query_engine.model.ProfileFilters;
import com.bituan.intelligence_query_engine.model.ProfilesPagination;
import com.bituan.intelligence_query_engine.model.entity.Profile;
import com.bituan.intelligence_query_engine.model.response.ProfilesResponse;
import com.bituan.intelligence_query_engine.model.request.AddProfileRequest;
import com.bituan.intelligence_query_engine.model.request.GetProfilesRequestQueryModel;
import com.bituan.intelligence_query_engine.model.response.ProfileResponse;
import com.bituan.intelligence_query_engine.repository.ProfileRepository;
import com.bituan.intelligence_query_engine.service.profile.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;


@RequiredArgsConstructor
@CrossOrigin("*")
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfilesResponse> getProfiles (ProfileFilters filters, ProfilesPagination pagination) {
        GetProfilesRequestQueryModel queryParams = new GetProfilesRequestQueryModel(filters, pagination);

        return new ResponseEntity<>(profileService.getProfiles(queryParams), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> addProfile (@RequestBody AddProfileRequest body) {
        return new ResponseEntity<>(profileService.addProfile(body.getName()), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<ProfilesResponse> getProfilesByNaturalLanguage (@RequestParam("q") String query, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) {
        return new ResponseEntity<>(profileService.getProfilesByNaturalLanguage(query, page, limit), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile (@PathVariable("id") String id) {
        return new ResponseEntity<>(profileService.getProfile(id), HttpStatus.OK);
    }

    @GetMapping("/export")
    public void exportProfiles (@RequestParam(name = "format", defaultValue = "csv") String format, ProfileFilters filters, HttpServletResponse response) {
        if (!format.equalsIgnoreCase("csv")) {
            throw new BadRequest("Only csv export is supported");
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"profiles_%s.csv\""
                .formatted(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)));

        try (PrintWriter writer = response.getWriter()) {
            writer.println("id,name,gender,gender_probability,age,age_group,country_id,country_name,country_probability,created_at");

            List<Profile> profiles = profileService.getProfiles(filters);

            for (Profile profile : profiles) {
                String row = String.format(Locale.ROOT, "%s,%s,%s,%f,%d,%s,%s,%s,%f,%s",
                        profile.getId(),
                        profile.getName(),
                        profile.getGender(),
                        profile.getGenderProbability(),
                        profile.getAge(),
                        profile.getAgeGroup(),
                        profile.getCountryId(),
                        profile.getCountryName(),
                        profile.getCountryProbability(),
                        profile.getCreatedAt()
                );
                writer.println(row);
            }
        } catch (IOException e) {
            throw new ServerException("Unable to export as csv");
        }

    }
}
