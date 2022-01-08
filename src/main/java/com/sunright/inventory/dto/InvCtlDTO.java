package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class InvCtlDTO extends BaseDTO {
    private BigDecimal stockDepn;
    private BigDecimal provAge;
}
