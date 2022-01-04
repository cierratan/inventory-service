package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class UomDTO extends BaseDTO {
    @NotBlank
    private String uomFrom;

    @NotBlank
    private String uomTo;
    private String fromDescription;
    private String toDescription;
    private BigDecimal uomFactor;
}
