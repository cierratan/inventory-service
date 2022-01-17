package com.sunright.inventory.dto.msr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class MsrDTO extends BaseDTO {
    private String msrNo;
    private Long version;

    private String supplierCode;
    private String docmNo;
    private String batchId;
    private String originator;
    private String mrvNo;
    private String currencyCode;

    private BigDecimal currencyRate;

    private Set<MsrDetailDTO> msrDetails;

    private String message;
}
