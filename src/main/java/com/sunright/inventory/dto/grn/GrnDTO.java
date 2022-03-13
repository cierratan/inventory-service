package com.sunright.inventory.dto.grn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties("recdDate")
@Getter
@Setter
@Builder
public class GrnDTO extends InvBaseDTO {

    private Long id;

    @NotBlank(message = "Grn No Can Not be Blank !")
    @Size(max = 15, message = "Maximum Grn No {max} characters")
    private String grnNo;

    @NotBlank(message = "Sub Type Can Not be Blank !")
    @Size(max = 10, message = "Maximum Sub Type {max} characters")
    private String subType;

    private String poNo;
    private String doNo;
    private String supplierCode;
    private String currencyCode;
    private BigDecimal currencyRate;

    @JsonIgnore
    private Date recdDate;

    private String statuz;
    private String entryUser;
    private Date entryDate;
    private Date closedDate;
    private String docmType;
    private String requestor;
    private String approver;
    private Date approvalDate;
    private String reqSubmitNo;
    private Date reqSubmitDate;
    private String poRemarks;
    private String supplierName;
    private String buyer;
    private Date rlseDate;
    protected Set<GrnDetDTO> grnDetails;

    private String msrNo;
}
