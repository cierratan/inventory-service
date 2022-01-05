package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunright.inventory.entity.enums.Closure;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class DraftPurDTO extends BaseDTO {

    private String poNo;
    private String docmType;
    private String poType;
    private String poClass;
    private String supplierCode;
    private String oriSupplierCode;
    private Date orderDate;
    private Date rlseDate;
    private String quotationNo;
    private String prNo;
    private String soNo;
    private String doNo;
    private String oiNo;
    private String cooNo;
    private String buyer;
    private String shiptoCustomerCode;
    private String shiptoName;
    private String shiptoAddress1;
    private String shiptoAddress2;
    private String shiptoAddress3;
    private String shiptoAddress4;
    private String shiptoPCode;
    private String shiptoContact;
    private String shiptoTelNo;
    private String shiptoFaxNo;
    private String billtoCompCode;
    private String billtoPlantNo;
    private String billtoContact;
    private String billtoTelNo;
    private String billtoFaxNo;
    private String contact;
    private String divCode;
    private String deptCode;
    private String paymentInd;
    private String paymentTerm;
    private String paymentDesc;
    private String shippingTerm;
    private String poInd;
    private String printInd;
    private Closure openClose;
    private String closeType;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date closeDate;

    private String forwarder;
    private String deliveryMode;
    private String currencyCode;
    private BigDecimal currencyRate;
    private BigDecimal carriage;
    private BigDecimal service;
    private BigDecimal insurance;
    private BigDecimal miscCharges;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private String discountDesc;
    private BigDecimal gstPct;
    private BigDecimal gstAmt;
    private String printRev;
    private String approver;
    private Date approvalDate;
    private String finalApprover;
    private Date finalApprovalDate;
    private String revNo;
    private Date revDate;
    private String lastModifiedUser;
    private Date lastModifiedDate;
    private String entryUser;
    private Date entryDate;
    private BigDecimal poAmount;
    private String poSubType;
    private BigDecimal specialDiscPct;
    private BigDecimal specialDiscAmt;
    private String remakrs;
}
