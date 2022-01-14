package com.sunright.inventory.dto.msr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class MsrDetailDTO {
    private String msrNo;
    private int seqNo;

    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;
    private String batchNo;
    private String projectNo;
    private String grnType;
    private String grnNo;
    private Integer grndetSeqNo;
    private BigDecimal grndetRecdQty;
    private String supplierRetn;
    private BigDecimal retnQty;
    private BigDecimal retnPrice;
    private String retnType;
    private String retnAction;
    private String uom;
    private BigDecimal recdQty;
    private BigDecimal recdPrice;
    private String currencyCode;
    private BigDecimal currencyRate;
    private String mrvNo;
    private String remarks;
}
