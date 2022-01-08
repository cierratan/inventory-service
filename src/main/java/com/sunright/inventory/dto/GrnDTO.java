package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
public class GrnDTO extends BaseDTO {

    private Long id;
    private String grnNo;
    private String subType;

    private String poNo;
    private String doNo;
    private String supplierCode;
    private String currencyCode;
    private BigDecimal currencyRate;
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
    private List<GrnDetDTO> grnDetList;
}
