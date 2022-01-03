package com.sunright.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class GrnDTO extends BaseDTO {

    private String grnNo;
    private String subType;

    private String poNo;
    private String doNo;
    private String supplierCode;
    private String currencyCode;
    private BigDecimal currencyRate;
    private Date recdDate;
//    private String status;
    private String entryUser;
    private Date entryDate;
    private Date closedDate;
    private String docmType;
    private String requestor;
    private String approver;
    private Date approvalDate;
    private String reqSubmitNo;
    private Date reqSubmitDate;
    private List<GrnDetDTO> grnDetList;
}