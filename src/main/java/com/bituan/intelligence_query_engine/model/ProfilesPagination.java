package com.bituan.intelligence_query_engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfilesPagination {
    private String sort_by;
    private String order;
    private Integer page;
    private Integer limit;
}
