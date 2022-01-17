package com.sunright.inventory.dto.supplier;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class SupplierDTO extends BaseDTO {

    private String supplierCode;
    private String name;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String pCode;
    private String telNo;
    private String faxNo;
    private String contact;
    private String contactPosition;
    private String buyer;
    private String currencyCode;
    private BigDecimal creditLimit;
    private Integer paymentTerm;
    private String shippingTerm;
    private BigDecimal lastTranAmt;
    private Date lastTranDate;
    private BigDecimal balbf;
}
