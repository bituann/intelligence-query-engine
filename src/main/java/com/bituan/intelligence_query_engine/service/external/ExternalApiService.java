package com.bituan.intelligence_query_engine.service.external;

import com.bituan.intelligence_query_engine.model.response.AgifyApiResponse;
import com.bituan.intelligence_query_engine.model.response.GenderizeApiResponse;
import com.bituan.intelligence_query_engine.model.response.NationalizeApiResponse;

public interface ExternalApiService {
    GenderizeApiResponse genderize(String name);
    AgifyApiResponse agify(String name);
    NationalizeApiResponse nationalize(String name);
}
