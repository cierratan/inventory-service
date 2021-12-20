package com.sunright.inventory.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SearchRequest {
    private List<Filter> filters;

    private int offset;
    private int limit = 20;
}
