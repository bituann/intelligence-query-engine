package com.bituan.intelligence_query_engine.model.request;

import com.bituan.intelligence_query_engine.model.ProfileFilters;
import com.bituan.intelligence_query_engine.model.ProfilesPagination;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetProfilesRequestQueryModel {
    private ProfileFilters filters;
    private ProfilesPagination pagination;
}
