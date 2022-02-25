package com.sunright.inventory.dto.grn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class GrnDetDTO extends BaseDTO {

    private Long id;

    @NotBlank(message = "Grn No Can Not be Blank !")
    private String grnNo;

    private String subType;
    private Integer seqNo;
    private String poNo;

    private String doNo;
    private String itemNo;

    @NotBlank(message = "Part No. Can Not be Blank !")
    private String partNo;

    @NotBlank(message = "Location Can Not be Blank !")
    private String loc;

    @NotEmpty(message = "Item Type Can Not be Blank !")
    private Integer itemType;

    private String projectNo;
    private Integer poRecSeq;
    private String sivNo;
    private String uom;
    private Date recdDate;

    private BigDecimal recdQty;

    @NotEmpty(message = "Recd Price Can Not be Blank !")
    private BigDecimal recdPrice;

    @NotNull(message = "Po Price Can Not be Blank !")
    private BigDecimal poPrice;

    @NotEmpty(message = "Issued Qty Can Not be Blank !")
    private BigDecimal issuedQty;

    @NotEmpty(message = "Qty per label cannot be empty or zero!")
    private BigDecimal labelQty;

    @NotEmpty(message = "Std Pack Qty Can Not be Blank !")
    private BigDecimal stdPackQty;
    private BigDecimal apRecdQty;
    private String remarks;

    @NotEmpty(message = "Order Qty Can Not be Blank !")
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
}
