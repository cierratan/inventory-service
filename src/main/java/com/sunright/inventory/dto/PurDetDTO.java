package com.sunright.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class PurDetDTO extends BaseDTO {

    private String poNo;
    private Integer recSeq;
    private Integer seqNo;
    private String projectNo;
    private String itemNo;
    private Integer itemType;
    private String partNo;
    private String loc;
    private String uom;
    private String stockStatus;
    private String orderNo;
    private Date dueDate;
    private BigDecimal orderQty;
    private BigDecimal unitPrice;
    private Date rlseDate;
    private BigDecimal rlseQty;
    private Date recdDate;
    private BigDecimal recdQty;
    private BigDecimal recdPrice;
    private BigDecimal resvQty;
    private String invUom;
    private BigDecimal stdPackQty;
    private BigDecimal stdMaterial;
    private BigDecimal freight;
    private BigDecimal handling;
    private BigDecimal customDuty;
    private String anyDiscount;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private Date mbiDate;
    private String buyFlag;
    private String advStatus;
    private String remarks;
    private BigDecimal apRecdQty;

    private String mslCode;
    private String codeDesc;
}
