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
    private int grnDetSeqNo;
    private BigDecimal grnDetRecdPrice;
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
}
