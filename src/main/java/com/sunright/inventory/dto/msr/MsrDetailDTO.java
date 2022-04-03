package com.sunright.inventory.dto.msr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import com.sunright.inventory.entity.enums.ReturnAction;
import com.sunright.inventory.entity.enums.ReturnType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class MsrDetailDTO extends InvBaseDTO {
    private String msrNo;

    @NotBlank
    private Integer seqNo;

    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;

    @NotBlank
    private String batchNo;

    private String projectNo;
    private String grnType;
    private String grnNo;
    private Integer grndetSeqNo;
    private BigDecimal grndetRecdQty;
    private String supplierRetn;

    @NotBlank
    private BigDecimal retnQty;

    @NotBlank
    private BigDecimal retnPrice;

    @NotBlank
    private ReturnType retnType;

    @NotBlank
    private ReturnAction retnAction;

    private String uom;
    private BigDecimal recdQty;
    private BigDecimal recdPrice;
    private String currencyCode;
    private BigDecimal currencyRate;
    private String remarks;

    private String mrvNo;
    private Integer mrvSeqNo;
    private String mrvProjectNo;
    private String mrvDocmNo;
    private String mrvSwap;
}
