package com.sunright.inventory.dto.grn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.BaseDTO;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class GrnDetDTO extends InvBaseDTO {

    private Long id;

    @NotBlank(message = "Grn No Can Not be Blank !")
    @Size(max = 15, message = "Maximum Grn No {max} characters")
    private String grnNo;

    @NotBlank(message = "Sub Type Can Not be Blank !")
    @Size(max = 10, message = "Maximum Sub Type {max} characters")
    private String subType;

    @NotBlank
    @Size(max = 3, message = "Maximum Seq No {max} characters")
    private Integer seqNo;

    private String poNo;
    private String doNo;
    private String itemNo;

    @NotBlank(message = "Part No. Can Not be Blank !")
    private String partNo;

    @NotBlank(message = "Location Can Not be Blank !")
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
