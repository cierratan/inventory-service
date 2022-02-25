package com.sunright.inventory.dto.mrv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class MrvDetailDTO extends InvBaseDTO {

    private String companyCode;
    private Integer plantNo;
    private String mrvNo;

    @NotBlank
    private Integer seqNo;

    @NotBlank
    private String itemNo;
    private String partNo;

    @NotBlank
    private String loc;

    @NotBlank
    private Integer itemType;
    private Long batchNo;

    @NotBlank
    private String projectNo;

    @NotBlank
    private String sivNo;
    private String uom;

    @NotBlank
    private BigDecimal recdQty;
    private BigDecimal recdPrice;
    private String docmNo;
    private String tranType;
    private String saleType;
    private String replace;
    private String msrStatus;

    @NotBlank
    private BigDecimal labelQty;
    private String remarks;

    private BigDecimal issuedQty;
}
