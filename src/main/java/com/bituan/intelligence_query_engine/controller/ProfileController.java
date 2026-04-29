package com.bituan.intelligence_query_engine.controller;

import com.bituan.intelligence_query_engine.model.ProfileFilters;
import com.bituan.intelligence_query_engine.model.ProfilesPagination;
import com.bituan.intelligence_query_engine.model.response.ProfilesResponse;
import com.bituan.intelligence_query_engine.model.request.AddProfileRequest;
import com.bituan.intelligence_query_engine.model.request.GetProfilesRequestQueryModel;
import com.bituan.intelligence_query_engine.model.response.ProfileResponse;
import com.bituan.intelligence_query_engine.repository.ProfileRepository;
import com.bituan.intelligence_query_engine.service.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
}
