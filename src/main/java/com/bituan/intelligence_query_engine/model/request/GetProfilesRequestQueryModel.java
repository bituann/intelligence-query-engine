package com.bituan.intelligence_query_engine.model;

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
