package com.sunright.inventory.dto.siv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class SIVDetailDTO extends InvBaseDTO {

    private String subType;
    private String sivNo;
    private int seqNo;
    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;
    private String uom;
    private Long batchNo;
    private String poNo;
    private String prNo;
    private String grnNo;
    private Integer grndetSeqNo;
    private BigDecimal grndetRecdPrice;
    private BigDecimal issuedQty;
    private BigDecimal issuedPrice;
    private String saleType;
    private String docmNo;
    private BigDecimal extraQty;
    private String remarks;

    private String batchLoc;
    private BigDecimal batchQty;
    private BigDecimal bomPickQty;
    private BigDecimal bomShortQtyL;
    private BigDecimal bomShortQtyF;

    private String batchDesc;
    private String batchNoLoc;

    private BigDecimal issuedQty1;
    private BigDecimal issuedQty2;
    private BigDecimal issuedQty3;
    private BigDecimal issuedQty4;
    private BigDecimal issuedQty5;

    private String projectNo1;
    private String projectNo2;
    private String projectNo3;
    private String projectNo4;
    private String projectNo5;

    private BigDecimal issuedQtyA;
    private BigDecimal issuedQtyB;
    private BigDecimal issuedQtyC;
    private BigDecimal issuedQtyD;
    private BigDecimal issuedQtyE;

    private BigDecimal bomQtyA;
    private BigDecimal bomQtyB;
    private BigDecimal bomQtyC;
    private BigDecimal bomQtyD;
    private BigDecimal bomQtyE;
}
