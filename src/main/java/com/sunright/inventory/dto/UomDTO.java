package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class UomDTO extends BaseDTO {
    private String uomFrom;
    private String uomTo;
    private String fromDescription;
    private String toDescription;
    private BigDecimal uomFactor;
}
