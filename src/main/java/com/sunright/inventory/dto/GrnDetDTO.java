package com.sunright.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
public class GrnDetDTO extends BaseDTO {

    private String grnNo;
    private String subType;

    private Integer seqNo;
    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;
    private String projectNo;
    private Integer poRecSeq;
    private String sivNo;
    private String uom;
    private Date recdDate;
    private BigDecimal recdQty;
    private BigDecimal recdPrice;
    private BigDecimal poPrice;
    private BigDecimal issuedQty;
    private BigDecimal labelQty;
    private BigDecimal stdPackQty;
    private BigDecimal apRecdQty;
    private String remarks;
}
