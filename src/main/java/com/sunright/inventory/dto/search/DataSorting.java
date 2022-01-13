package com.sunright.inventory.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.search.SortOption;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@Builder
public class DataSorting {
    private String field;
    private SortOption sort;
}
