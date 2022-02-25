package com.sunright.inventory.dto.mrv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class MrvDTO extends InvBaseDTO {
    private String mrvNo;
    private String statuz;
    private String currencyCode;
    private BigDecimal currencyRate;
    private Set<MrvDetailDTO> mrvDetails;
}
