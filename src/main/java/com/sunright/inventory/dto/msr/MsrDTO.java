package com.sunright.inventory.dto.msr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class MsrDTO extends InvBaseDTO {
    private String msrNo;
    private Long version;

    private String supplierCode;

    @NotBlank
    private String docmNo;

    private String batchId;

    @NotBlank
    private String originator;
    private String mrvNo;

    private String currencyCode;
    private BigDecimal currencyRate;

    private Set<MsrDetailDTO> msrDetails;

    private String message;
}
