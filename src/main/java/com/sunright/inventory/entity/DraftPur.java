package com.sunright.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "DRAFT_PUR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DraftPur extends BaseEntity {

    @EmbeddedId
    private DraftPurId id;

    /*@Version
    private Long version;*/

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
    private Integer billtoPlantNo;
    private String billtoContact;
    private String billtoTelNo;
    private String billtoFaxNo;
    private String contact;
    private String divCode;
    private String deptCode;
    private String paymentInd;
    private Integer paymentTerm;
    private String paymentDesc;
    private String shippingTerm;
    private String poInd;
    private String printInd;

    @Enumerated(EnumType.STRING)
    private Closure openClose;

    private String closeType;
    private Date closeDate;
    private String forwarder;
    private String deliveryMode;
    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    @Column(name = "CARRIAGE", precision = 16, scale = 4)
    private BigDecimal carriage;

    @Column(name = "SERVICE", precision = 16, scale = 4)
    private BigDecimal service;

    @Column(name = "INSURANCE", precision = 16, scale = 4)
    private BigDecimal insurance;

    @Column(name = "MISC_CHARGES", precision = 16, scale = 4)
    private BigDecimal miscCharges;

    @Column(name = "DISCOUNT_AMOUNT", precision = 16, scale = 4)
    private BigDecimal discountAmount;

    @Column(name = "DISCOUNT_PERCENT", precision = 8, scale = 4)
    private BigDecimal discountPercent;

    private String discountDesc;

    @Column(name = "GST_PCT", precision = 8, scale = 4)
    private BigDecimal gstPct;

    @Column(name = "GST_AMT", precision = 16, scale = 4)
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

    @Column(name = "PO_AMOUNT", precision = 16, scale = 4)
    private BigDecimal poAmount;

    private String poSubType;

    @Column(name = "SPECIAL_DISC_PCT", precision = 8, scale = 4)
    private BigDecimal specialDiscPct;

    @Column(name = "SPECIAL_DISC_AMT", precision = 16, scale = 4)
    private BigDecimal specialDiscAmt;

    private String remarks;

}
