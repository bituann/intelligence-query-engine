package com.bituan.intelligence_query_engine.service.profile;

import com.bituan.intelligence_query_engine.model.response.ProfilesResponse;
import com.bituan.intelligence_query_engine.model.request.GetProfilesRequestQueryModel;
import com.bituan.intelligence_query_engine.model.response.ProfileResponse;

public interface ProfileService {
    ProfilesResponse getProfiles(GetProfilesRequestQueryModel queryParams);
    ProfilesResponse getProfilesByNaturalLanguage(String query, Integer page, Integer limit);
    ProfileResponse addProfile (String name);
}
