package com.sunright.inventory.dto.grn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "recdDate")
@Getter
@Setter
@Builder
public class GrnDetDTO extends InvBaseDTO {

    private Long id;
    private String grnNo;
    private String subType;
    private Integer seqNo;
    private String poNo;
    private String doNo;
    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;
    private String projectNo;
    private Integer poRecSeq;
    private String sivNo;
    private String uom;
    @JsonIgnore
    private Date recdDate;
    private BigDecimal recdQty;
    private BigDecimal recdPrice;
    private BigDecimal poPrice;
    private BigDecimal issuedQty;
    private BigDecimal labelQty;
    private BigDecimal stdPackQty;
    private BigDecimal apRecdQty;
    private String remarks;
    private BigDecimal orderQty;
    private Date dueDate;
    private String mslCode;
    private String description;
    private BigDecimal resvQty;
    private String invUom;
    private BigDecimal retnQty;
    private BigDecimal retnPrice;
    private Integer dateCode;
    private String mrvNo;

    private BigDecimal newStdMaterial;
    private BigDecimal newCostVar;
    private BigDecimal convQoh;
}
