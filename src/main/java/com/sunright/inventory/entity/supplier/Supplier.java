package com.sunright.inventory.entity.supplier;

import com.sunright.inventory.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "SUPPLIER")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Supplier extends BaseEntity {

    @EmbeddedId
    private SupplierId id;

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

    @Column(name = "CREDIT_LIMIT", precision = 14, scale = 2)
    private BigDecimal creditLimit;

    private Integer paymentTerm;
    private String shippingTerm;

    @Column(name = "LAST_TRAN_AMT", precision = 14, scale = 2)
    private BigDecimal lastTranAmt;

    private Date lastTranDate;

    @Column(name = "BALBF", precision = 14, scale = 2)
    private BigDecimal balbf;
}
