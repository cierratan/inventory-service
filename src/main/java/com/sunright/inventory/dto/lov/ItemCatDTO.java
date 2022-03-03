package com.sunright.inventory.dto.lov;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import com.sunright.inventory.entity.enums.MRPStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class ItemCatDTO extends InvBaseDTO {
    private String categoryCode;
    private String categorySubCode;
    private String categoryGroup;

    private String description;
    private String subDescription;

    private String mrpStatus;

    private BigDecimal designQtya;
    private BigDecimal designQtyb;
    private BigDecimal designQtyc;
    private BigDecimal designQtyd;

    private Integer mifA;
    private Integer mifB;
    private Integer mifC;
    private Integer mifD;
}
