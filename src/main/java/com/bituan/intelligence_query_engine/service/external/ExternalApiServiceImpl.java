package com.bituan.intelligence_query_engine.service.external;

import com.bituan.intelligence_query_engine.exception.ExternalApiException;
import com.bituan.intelligence_query_engine.model.response.AgifyApiResponse;
import com.bituan.intelligence_query_engine.model.response.GenderizeApiResponse;
import com.bituan.intelligence_query_engine.model.response.NationalizeApiResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ExternalApiServiceImpl implements ExternalApiService {
    @Override
    public GenderizeApiResponse genderize(String name) {
        RestClient restClient = RestClient.create();

        ResponseEntity<GenderizeApiResponse> response = restClient
                .get()
                .uri("https://api.genderize.io?name=" + name)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ExternalApiException(502, "Genderize returned an invalid response");
                })
                .toEntity(GenderizeApiResponse.class);

        if (!response.hasBody()) {
            throw new ExternalApiException(502, "Genderize returned an invalid response");
        }

        if (response.getBody().getCount() <= 0 || response.getBody().getGender() == null) {
            throw new ExternalApiException(502, "Genderize returned an invalid response");
        }

        return response.getBody();
    }

    @Override
    public AgifyApiResponse agify(String name) {
        RestClient restClient = RestClient.create();

        ResponseEntity<AgifyApiResponse> response = restClient
                .get()
                .uri("https://api.agify.io?name=" + name)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ExternalApiException(502, "Agify returned an invalid response");
                })
                .toEntity(AgifyApiResponse.class);

        if (!response.hasBody()) {
            throw new ExternalApiException(502, "Agify returned an invalid response");
        }

        return response.getBody();
    }

    @Override
    public NationalizeApiResponse nationalize(String name) {
        RestClient restClient = RestClient.create();

        ResponseEntity<NationalizeApiResponse> response = restClient
                .get()
                .uri("https://api.nationalize.io/?name=" + name)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ExternalApiException(502, "Nationalize returned an invalid response");
                })
                .toEntity(NationalizeApiResponse.class);

        if (!response.hasBody() || response.getBody().getCountry().isEmpty() || response.getBody().getCountry() == null) {
            throw new ExternalApiException(502, "Nationalize returned an invalid response");
        }

        return response.getBody();
    }
}
