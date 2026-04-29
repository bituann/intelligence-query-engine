package com.bituan.intelligence_query_engine.service.profile;

import com.bituan.intelligence_query_engine.model.ProfileFilters;
import com.bituan.intelligence_query_engine.model.entity.Profile;
import com.bituan.intelligence_query_engine.model.response.ProfilesResponse;
import com.bituan.intelligence_query_engine.model.request.GetProfilesRequestQueryModel;
import com.bituan.intelligence_query_engine.model.response.ProfileResponse;

import java.util.List;

public interface ProfileService {
    ProfilesResponse getProfiles(GetProfilesRequestQueryModel queryParams);
    List<Profile> getProfiles(ProfileFilters filters);
    ProfilesResponse getProfilesByNaturalLanguage(String query, Integer page, Integer limit);
    ProfileResponse getProfile(String id);
    ProfileResponse addProfile (String name);
}
