package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
public class BaseDTO {
    private String companyCode;
    private Integer plantNo;
}
